package com.mazhuo.airy.domain.repository

import android.os.Environment
import com.mazhuo.airy.domain.model.AiryFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalFileRepository @Inject constructor() {

    /**
     * 获取手机根目录 (一般为 /storage/emulated/0)
     */
    fun getStorageRootPath(): String {
        return Environment.getExternalStorageDirectory().absolutePath
    }

    /**
     * 获取本地文件列表
     */
    suspend fun fetchLocalFiles(targetPath: String): List<AiryFile> = withContext(Dispatchers.IO) {
        val path = targetPath.ifBlank { getStorageRootPath() }
        val directory = File(path)

        if (!directory.exists() || !directory.isDirectory) {
            return@withContext emptyList()
        }

        val files = directory.listFiles() ?: return@withContext emptyList()

        return@withContext files.map { file ->
            AiryFile(
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                size = if (file.isDirectory) 0L else file.length(),
                lastModified = file.lastModified()
            )
        }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }
}