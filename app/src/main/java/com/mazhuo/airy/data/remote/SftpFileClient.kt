package com.mazhuo.airy.data.remote

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.mazhuo.airy.data.local.entity.ServerConfig
import com.mazhuo.airy.domain.model.RemoteFile
import com.mazhuo.airy.domain.repository.FileClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Vector

class SftpFileClient : FileClient {
    private var session: Session? = null
    private var channelSftp: ChannelSftp? = null

    override suspend fun connect(config: ServerConfig): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsch = JSch()
            if (!config.privateKeyPath.isNullOrEmpty()) {
                jsch.addIdentity(config.privateKeyPath)
            }
            session = jsch.getSession(config.username, config.host, config.port)
            if (config.privateKeyPath.isNullOrEmpty()) {
                session?.setPassword(config.passwordEncrypted)
            }
            val configProperties = java.util.Properties().apply { put("StrictHostKeyChecking", "no") }
            session?.setConfig(configProperties)
            session?.connect(10000)
            channelSftp = session?.openChannel("sftp") as? ChannelSftp
            channelSftp?.connect()
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    override suspend fun listFiles(path: String): List<RemoteFile> = withContext(Dispatchers.IO) {
        val files = mutableListOf<RemoteFile>()
        val sftp = channelSftp ?: return@withContext files
        try {
            val vector = sftp.ls(path) as Vector<ChannelSftp.LsEntry>
            for (entry in vector) {
                if (entry.filename == "." || entry.filename == "..") continue
                files.add(
                    RemoteFile(
                        name = entry.filename,
                        path = if (path.endsWith("/")) "$path${entry.filename}" else "$path/${entry.filename}",
                        size = entry.attrs.size,
                        isDirectory = entry.attrs.isDir,
                        lastModified = entry.attrs.mTime * 1000L,
                        permissions = entry.attrs.permissionsString
                    )
                )
            }
        } catch (e: Exception) { e.printStackTrace() }
        return@withContext files
    }

    override suspend fun downloadFile(remotePath: String, localFile: File, progress: (Long) -> Unit) {
        withContext(Dispatchers.IO) {
            val sftp = channelSftp ?: return@withContext
            var mode = ChannelSftp.OVERWRITE
            var skipByte = 0L

            // 完美的断点续传：如果本地存在文件，采用 RESUME 模式
            if (localFile.exists() && localFile.length() > 0) {
                mode = ChannelSftp.RESUME
                skipByte = localFile.length()
            }

            FileOutputStream(localFile, true).use { outputStream ->
                sftp.get(remotePath, outputStream, SftpProgressMonitorImpl(skipByte, progress),mode,skipByte)
            }
        }
    }

    override suspend fun uploadFile(remotePath: String, localFile: File, progress: (Long) -> Unit) {
        // SFTP 上传同理，可以使用 ChannelSftp.RESUME 写入
    }

    override suspend fun deleteFile(remotePath: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try { channelSftp?.rm(remotePath); true } catch (e: Exception) { false }
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        channelSftp?.disconnect(); session?.disconnect()
        Unit
    }

    private class SftpProgressMonitorImpl(initialOffset: Long, val progress: (Long) -> Unit) : com.jcraft.jsch.SftpProgressMonitor {
        private var transferred: Long = initialOffset
        override fun init(op: Int, src: String?, dest: String?, max: Long) {}
        override fun count(count: Long): Boolean {
            transferred += count
            progress(transferred)
            return true
        }
        override fun end() {}
    }
}