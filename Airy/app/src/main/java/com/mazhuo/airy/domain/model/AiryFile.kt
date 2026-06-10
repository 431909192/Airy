package com.mazhuo.airy.domain.model

data class AiryFile (
    val name: String,         // 文件或文件夹名称
    val path: String,         // 绝对路径
    val isDirectory: Boolean, // 是否是文件夹
    val size: Long,           // 文件大小 (字节)
    val lastModified: Long    // 戳修改时间
)
