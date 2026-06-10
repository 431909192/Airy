package com.mazhuo.airy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mazhuo.airy.domain.model.ProtocolType
import com.mazhuo.airy.ui.viewmodel.AddServerViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServerScreen(
    onBackClick: () -> Unit,
    viewModel: AddServerViewModel = hiltViewModel()
) {
    // 监听保存成功事件，一旦成功自动返回上一页
    LaunchedEffect(Unit) {
        viewModel.saveSuccessEvent.collectLatest { success ->
            if (success) onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新增远程连接") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // 支持表单过长时滚动
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 协议选择标签
            Text("协议类型", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProtocolType.values().forEach { type ->
                    FilterChip(
                        selected = viewModel.protocolType == type,
                        onClick = { viewModel.onProtocolChange(type) },
                        label = { Text(type.name) }
                    )
                }
            }

            // 2. 输入表单群
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = { Text("连接别名 (如: 我的阿里云)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = viewModel.host,
                    onValueChange = { viewModel.host = it },
                    label = { Text("主机地址/IP") },
                    modifier = Modifier.weight(0.7f)
                )
                OutlinedTextField(
                    value = viewModel.port,
                    onValueChange = { viewModel.port = it },
                    label = { Text("端口") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.3f)
                )
            }

            OutlinedTextField(
                value = viewModel.username,
                onValueChange = { viewModel.username = it },
                label = { Text("用户名") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
                label = { Text("密码 / 密钥密码") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.rootPath,
                onValueChange = { viewModel.rootPath = it },
                label = { Text("根目录路径") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. 保存按钮
            Button(
                onClick = { viewModel.saveServer() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = viewModel.name.isNotBlank() && viewModel.host.isNotBlank() && viewModel.username.isNotBlank()
            ) {
                Text("保存连接", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}