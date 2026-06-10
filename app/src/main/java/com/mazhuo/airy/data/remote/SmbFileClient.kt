package com.mazhuo.airy.data.remote

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import com.mazhuo.airy.data.local.entity.ServerConfig
import com.mazhuo.airy.domain.model.RemoteFile
import com.mazhuo.airy.domain.repository.FileClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.util.EnumSet

class SmbFileClient : FileClient {
    private val smbClient = SMBClient()
    private var connection: Connection? = null
    private var session: Session? = null
    private var diskShare: DiskShare? = null
    private var currentShareName = ""

    override suspend fun connect(config: ServerConfig): Boolean = withContext(Dispatchers.IO) {
        try {
            connection = smbClient.connect(config.host, config.port)
            val authContext = AuthenticationContext(config.username, config.passwordEncrypted.toCharArray(), null)
            session = connection?.authenticate(authContext)

            // 默认解构SMB根路径提取 Share Name (例如 /SharedFolder/SubDir -> SharedFolder)
            val cleanPath = config.rootPath.trimStart('/')
            currentShareName = cleanPath.substringBefore('/')

            diskShare = session?.connectShare(currentShareName) as? DiskShare
            return@withContext diskShare != null
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    override suspend fun listFiles(path: String): List<RemoteFile> = withContext(Dispatchers.IO) {
        val files = mutableListOf<RemoteFile>()
        val share = diskShare ?: return@withContext files
        try {
            // 转换相对 SMB 共享目录路径
            val targetPath = path.removePrefix("/$currentShareName").trimStart('/')
            val list = share.list(targetPath)
            for (info in list) {
                if (info.fileName == "." || info.fileName == "..") continue
                val isDir = info.fileAttributes.and(FileAttributes.FILE_ATTRIBUTE_DIRECTORY.value) != 0L
                files.add(
                    RemoteFile(
                        name = info.fileName,
                        path = if (path.endsWith("/")) "$path${info.fileName}" else "$path/${info.fileName}",
                        size = info.endOfFile,
                        isDirectory = isDir,
                        lastModified = info.changeTime.toEpochMillis()
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext files
    }

    override suspend fun downloadFile(remotePath: String, localFile: File, progress: (Long) -> Unit) {
        withContext(Dispatchers.IO) {
            val share = diskShare ?: return@withContext
            val targetPath = remotePath.removePrefix("/$currentShareName").trimStart('/')

            var localFileSize = 0L
            if (localFile.exists()) {
                localFileSize = localFile.length()
            }

            // 打开远程 SMB 文件
            val remoteFile = share.openFile(
                targetPath,
                EnumSet.of(AccessMask.FILE_READ_DATA),
                EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN,
                null
            )

            remoteFile.use { smbFile ->
                RandomAccessFile(localFile, "rw").use { raf ->
                    raf.seek(localFileSize) // 定位本地文件断点

                    val buffer = ByteArray(4096)
                    var fileOffset = localFileSize
                    var bytesRead: Int

                    // 基于指定的字节偏置量向 SMB 服务器读取数据，天然支持断点续传
                    while (smbFile.read(buffer, fileOffset, 0, buffer.size).also { bytesRead = it } > 0) {
                        raf.write(buffer, 0, bytesRead)
                        fileOffset += bytesRead
                        progress(fileOffset)
                    }
                }
            }
        }
    }

    override suspend fun uploadFile(remotePath: String, localFile: File, progress: (Long) -> Unit) {
        withContext(Dispatchers.IO) {
            val share = diskShare ?: return@withContext
            val targetPath = remotePath.removePrefix("/$currentShareName").trimStart('/')

            val remoteFile = share.openFile(
                targetPath,
                EnumSet.of(AccessMask.FILE_WRITE_DATA),
                EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN_IF,
                null
            )

            remoteFile.use { smbFile ->
                RandomAccessFile(localFile, "r").use { raf ->
                    val buffer = ByteArray(4096)
                    var fileOffset = 0L // 基础上传逻辑（可根据 smbFile.fileInformation 进一步实现上传断点）
                    var bytesRead: Int
                    while (raf.read(buffer).also { bytesRead = it } != -1) {
                        smbFile.write(buffer, fileOffset, 0, bytesRead)
                        fileOffset += bytesRead
                        progress(fileOffset)
                    }
                }
            }
        }
    }

    override suspend fun deleteFile(remotePath: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val targetPath = remotePath.removePrefix("/$currentShareName").trimStart('/')
            diskShare?.rm(targetPath)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        diskShare?.close()
        session?.close()
        connection?.close()
        Unit
    }
}