package com.mazhuo.airy.domain.repository

import com.mazhuo.airy.data.local.entity.ServerConfig
import com.mazhuo.airy.domain.model.RemoteFile
import java.io.File


interface FileClient {
    suspend fun connect(config: ServerConfig): Boolean
    suspend fun disconnect()
    suspend fun listFiles(path: String): List<RemoteFile>
    suspend fun downloadFile(remotePath: String, localFile: File, progress: (Long) -> Unit)
    suspend fun uploadFile(remotePath: String, localFile: File, progress: (Long) -> Unit)
    suspend fun deleteFile(remotePath: String): Boolean
}