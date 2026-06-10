package com.mazhuo.airy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import android.widget.Toast
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mazhuo.airy.domain.model.ProtocolType
import com.mazhuo.airy.ui.home.model.HomeItem
import com.mazhuo.airy.ui.viewmodel.HomeViewModel
import com.mazhuo.airy.data.local.entity.ServerConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onAddConnectionClick: () -> Unit,
               onServerClick: (ServerConfig) -> Unit,
               onLocalClick:()-> Unit,
               viewModel: HomeViewModel = hiltViewModel()){
    val context = LocalContext.current
    val items by viewModel.homeItems.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("文件管理") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                when (item) {
                    is HomeItem.LocalStorage -> {
                        BaseGridCard(title = "本地存储", icon = Icons.Default.Folder) {
                            onLocalClick()
                        }
                    }
                    is HomeItem.SavedServer -> {
                        val config = item.config
                        BaseGridCard(
                            title = config.name,
                            icon = when(config.protocolType){
                                ProtocolType.SFTP -> Icons.Default.Terminal
                                else -> Icons.Default.Cloud
                            },
                            subtitle = "${config.username}@${config.host}"
                        ) {
                            onServerClick(config)
                        }
                    }
                    is HomeItem.AddConnection -> {
                        Card(
                            onClick = onAddConnectionClick,
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("新增远程连接", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BaseGridCard(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(120.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

/**
 * 根据 Room 中存储的字符串协议类型分配图标
 */
fun getServerIcon(type: String): ImageVector {
    return when (type.uppercase()) {
        "SFTP" -> Icons.Default.Terminal
        "SMB" -> Icons.Default.Computer
        "WEBDAV" -> Icons.Default.Cloud
        "FTP", "FTPS" -> Icons.Default.Dns
        else -> Icons.Default.Cloud
    }
}