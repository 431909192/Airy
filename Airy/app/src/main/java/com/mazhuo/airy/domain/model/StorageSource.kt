package com.mazhuo.airy.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.ui.graphics.vector.ImageVector

sealed class StorageSource(val name: String, val icon: ImageVector) {
    object Local : StorageSource("本地存储", Icons.Default.Folder)
    object SFTP : StorageSource("SFTP 服务器", Icons.Default.Terminal)
    object SMB : StorageSource("Samba (局域网共享)", Icons.Default.Computer)
    object WebDAV : StorageSource("WebDAV 云盘", Icons.Default.Cloud)
    object FTP : StorageSource("FTP / FTPS", Icons.Default.Dns)
}