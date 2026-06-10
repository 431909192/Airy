package com.mazhuo.airy.domain.repository

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.Session
import com.jcraft.jsch.JSch
import com.mazhuo.airy.domain.model.AiryFile
import com.mazhuo.airy.data.local.entity.ServerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Vector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteFileRepository @Inject constructor() {

    private var currentSession: Session? = null
    private var currentChannel: ChannelSftp? = null
    private var currentConfigId: Long = -1L

    /**
     * 核心连接方法 (支持 SFTP)
     */
    private suspend fun initSftpClient(config: ServerConfig): ChannelSftp = withContext(Dispatchers.IO) {
        if (currentChannel?.isConnected == true && currentConfigId == config.id) {
            return@withContext currentChannel!!
        }

        // 断开旧连接
        disconnect()

        val jsch = JSch()
        // 如果有私钥路径，在此处配置：jsch.addIdentity(config.privateKeyPath)

        val session = jsch.getSession(config.username, config.host, config.port)
        session.setPassword(config.passwordEncrypted)
        val prop = java.util.Properties()
        prop["StrictHostKeyChecking"] = "no" // 绕过已知主机检查
        session.setConfig(prop)
        session.connect(10000) // 10秒连接超时
        val channel = session.openChannel("sftp") as ChannelSftp
        channel.connect(5000)
        currentSession = session
        currentChannel = channel
        currentConfigId = config.id

        return@withContext channel
    }

    suspend fun fetchRemoteFiles(config: ServerConfig, targetPath: String): List<AiryFile> = withContext(Dispatchers.IO) {
        try {
            val channel = initSftpClient(config)
            val fileList = mutableListOf<AiryFile>()

            // 执行 SFTP ls 命令
            val vector: Vector<*> = channel.ls(targetPath.ifBlank { config.rootPath })

            for (obj in vector) {
                val entry = obj as? ChannelSftp.LsEntry ?: continue
                if (entry.filename == "." || entry.filename == "..") continue

                val attrs = entry.attrs
                val fullPath = if (targetPath.endsWith("/")) targetPath + entry.filename else "$targetPath/${entry.filename}"

                fileList.add(
                    AiryFile(
                        name = entry.filename,
                        path = fullPath,
                        isDirectory = attrs.isDir,
                        size = attrs.size,
                        lastModified = attrs.mTime * 1000L // 转换为毫秒
                    )
                )
            }
            // 排序：文件夹优先，其后按名称字母排序
            return@withContext fileList.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        } catch (e: Exception) {
            e.printStackTrace()
            throw e // 抛给 ViewModel 层处理异常提示
        }
    }

    fun disconnect() {
        currentChannel?.disconnect()
        currentSession?.disconnect()
        currentChannel = null
        currentSession = null
        currentConfigId = -1L
    }
}