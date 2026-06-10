package com.mazhuo.airy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_servers")
data class ServerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,       // 服务器别名，例如 "我的阿里云"
    val type: String,       // 协议类型: "SFTP", "SMB", "WEBDAV", "FTP"
    val host: String,       // IP 或 域名
    val port: Int,
    val username: String
)