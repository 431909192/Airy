package com.mazhuo.airy.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mazhuo.airy.domain.model.AiryFile
import com.mazhuo.airy.ui.viewmodel.FileBrowserViewModel
import com.mazhuo.airy.ui.viewmodel.FileUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(titleName: String, onExitScreen: () -> Unit, viewModel: FileBrowserViewModel = hiltViewModel()) {
    BackHandler {
        if (!viewModel.popBackIfNeeded()) {
            onExitScreen()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(titleName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = viewModel.currentPath,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { if (!viewModel.popBackIfNeeded()) onExitScreen() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = viewModel.uiState) {
                is FileUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is FileUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(state.msg, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadFiles() }) { Text("重试") }
                    }
                }
                is FileUiState.Success -> {
                    if (state.files.isEmpty()) {
                        Text("空目录", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.outline)
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.files) { file ->
                                FileItemRow(file = file, onClick = {
                                    if (file.isDirectory) {
                                        viewModel.enterDirectory(file.path)
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FileItemRow(file: AiryFile, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(file.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            if (!file.isDirectory) {
                Text(formatFileSize(file.size), style = MaterialTheme.typography.bodySmall)
            }
        },
        leadingContent = {
            Icon(
                imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                contentDescription = null,
                tint = if (file.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    )
}

fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(Locale.US, "%.2f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
