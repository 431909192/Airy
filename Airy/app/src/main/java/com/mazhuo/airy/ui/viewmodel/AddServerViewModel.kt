package com.mazhuo.airy.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mazhuo.airy.data.local.dao.ServerConfigDao
import com.mazhuo.airy.data.local.entity.ServerConfig
import com.mazhuo.airy.domain.model.ProtocolType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddServerViewModel @Inject constructor(
    private val serverConfigDao: ServerConfigDao
) : ViewModel() {

    // 使用 Compose State 管理表单输入，响应极快
    var name by mutableStateOf("")
    var host by mutableStateOf("")
    var port by mutableStateOf("22") // 默认 SFTP 端口
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var protocolType by mutableStateOf(ProtocolType.SFTP)
    var rootPath by mutableStateOf("/")

    // 用于通知 UI 保存成功的单发事件流
    private val _saveSuccessEvent = MutableSharedFlow<Boolean>()
    val saveSuccessEvent: SharedFlow<Boolean> = _saveSuccessEvent

    fun onProtocolChange(type: ProtocolType) {
        protocolType = type
        // 顺手帮你做个小优化：切换协议时自动更正默认端口
        port = when (type) {
            ProtocolType.SFTP -> "22"
            // 如果你的 ProtocolType 有其他类型，可以在这里配置默认端口
            else -> "80"
        }
    }

    fun saveServer() {
        if (name.isBlank() || host.isBlank() || username.isBlank()) return

        viewModelScope.launch {
            val config = ServerConfig(
                name = name,
                host = host,
                port = port.toIntOrNull() ?: 22,
                username = username,
                passwordEncrypted = password, // 🔒 暂存明文，后续你可以对接加密算法
                protocolType = protocolType,
                rootPath = rootPath,
                lastConnected = System.currentTimeMillis()
            )
            serverConfigDao.insertServer(config)
            _saveSuccessEvent.emit(true) // 发射保存成功通知
        }
    }
}