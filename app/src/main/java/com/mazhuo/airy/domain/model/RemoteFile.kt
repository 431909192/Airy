package com.mazhuo.airy.domain.model

data class RemoteFile (
    val name: String,
    val path: String,
    val size: Long,
    val isDirectory: Boolean,
    val lastModified: Long,
    val permissions: String = ""
)

