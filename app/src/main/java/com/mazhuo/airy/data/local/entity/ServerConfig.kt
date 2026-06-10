package com.mazhuo.airy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mazhuo.airy.domain.model.ProtocolType

@Entity(tableName = "server_configs")
data class ServerConfig(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val host: String,
    val port: Int,
    val username: String,
    val passwordEncrypted: String,
    val protocolType: ProtocolType,
    val rootPath: String = "/",
    val privateKeyPath: String? = null,
    val lastConnected: Long = 0
)
