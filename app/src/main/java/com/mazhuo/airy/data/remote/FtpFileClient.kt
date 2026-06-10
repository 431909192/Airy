package com.mazhuo.airy.data.remote

import com.mazhuo.airy.data.local.entity.ServerConfig
import com.mazhuo.airy.domain.model.ProtocolType
import com.mazhuo.airy.domain.model.RemoteFile
import com.mazhuo.airy.domain.repository.FileClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPSClient
import java.io.File
import java.io.RandomAccessFile

class FtpFileClient : FileClient {
    private var ftpClient: FTPClient? = null

    override suspend fun connect(config: ServerConfig): Boolean = withContext(Dispatchers.IO) {
        try {

            ftpClient = if (config.protocolType == ProtocolType.FTPS) {
                FTPSClient().apply {
                    controlKeepAliveReplyTimeout = 1000
                }
            } else {
                FTPClient()
            }

            ftpClient?.connect(config.host, config.port)
            val loginSuccess = ftpClient?.login(config.username, config.passwordEncrypted) ?: false

            if (loginSuccess) {
                ftpClient?.enterLocalPassiveMode()
                ftpClient?.setFileType(FTP.BINARY_FILE_TYPE)
            }
            return@withContext loginSuccess
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    override suspend fun listFiles(path: String): List<RemoteFile> = withContext(Dispatchers.IO) {
        val files = mutableListOf<RemoteFile>()
        val client = ftpClient ?: return@withContext files
        try {
            val ftpFiles: Array<FTPFile> = client.listFiles(path)
            for (file in ftpFiles) {
                if (file.name == "." || file.name == "..") continue
                files.add(
                    RemoteFile(
                        name = file.name,
                        path = if (path.endsWith("/")) "$path${file.name}" else "$path/${file.name}",
                        size = file.size,
                        isDirectory = file.isDirectory,
                        lastModified = file.timestamp.timeInMillis,
                        permissions = file.rawListing.substringBefore(" ", "")
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
            val client = ftpClient ?: return@withContext
            var localFileSize = 0L

            // 断点续传核心逻辑：检查本地文件是否存在，并设置偏移量
            if (localFile.exists()) {
                localFileSize = localFile.length()
                client.setRestartOffset(localFileSize) // 通知FTP服务器从该位置开始流式传输
            }

            RandomAccessFile(localFile, "rw").use { raf ->
                raf.seek(localFileSize) // 移动本地指针到文件末尾

                client.retrieveFileStream(remotePath).use { inputStream ->
                    if (inputStream == null) return@withContext
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    var totalTransferred = localFileSize

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        raf.write(buffer, 0, bytesRead)
                        totalTransferred += bytesRead
                        progress(totalTransferred)
                    }
                    client.completePendingCommand() // 必须完成挂起命令以重置FTP状态机
                }
            }
        }
    }

    override suspend fun uploadFile(remotePath: String, localFile: File, progress: (Long) -> Unit) {
        withContext(Dispatchers.IO) {
            val client = ftpClient ?: return@withContext
            var offset = 0L

            // FTP 支持通过 appendFileStream 追加实现续传
            val outputStream = if (client.changeWorkingDirectory(remotePath)) {
                // 简化处理，实际需要检测远程文件大小
                client.appendFileStream(remotePath)
            } else {
                client.storeFileStream(remotePath)
            }

            outputStream?.use { fos ->
                RandomAccessFile(localFile, "r").use { raf ->
                    raf.seek(offset)
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    var totalTransferred = offset
                    while (raf.read(buffer).also { bytesRead = it } != -1) {
                        fos.write(buffer, 0, bytesRead)
                        totalTransferred += bytesRead
                        progress(totalTransferred)
                    }
                }
                client.completePendingCommand()
            }
        }
    }

    override suspend fun deleteFile(remotePath: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext ftpClient?.deleteFile(remotePath) ?: false
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        if (ftpClient?.isConnected == true) {
            ftpClient?.logout()
            ftpClient?.disconnect()
        }
    }
}