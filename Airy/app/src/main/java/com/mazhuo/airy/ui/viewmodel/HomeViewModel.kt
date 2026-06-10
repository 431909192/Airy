package com.mazhuo.airy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mazhuo.airy.data.local.dao.ServerConfigDao
import com.mazhuo.airy.data.local.entity.ServerConfig
import com.mazhuo.airy.domain.model.ProtocolType
import com.mazhuo.airy.ui.home.model.HomeItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val serverConfigDao: ServerConfigDao // 👈 注入你真正的 Dao
) : ViewModel() {

    val homeItems: StateFlow<List<HomeItem>> = serverConfigDao.getAllServers() // 👈 读取配置流
        .map { configs ->
            val items = mutableListOf<HomeItem>()
            items.add(HomeItem.LocalStorage)
            items.addAll(configs.map { HomeItem.SavedServer(it) }) // 👈 映射新模型
            items.add(HomeItem.AddConnection)
            items
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(HomeItem.LocalStorage, HomeItem.AddConnection)
        )

    // 临时测试方法：往数据库塞入一条真实的 SFTP 配置
    fun addDebugServer() {
        viewModelScope.launch {
            serverConfigDao.insertServer(
                ServerConfig(
                    name = "我的群晖 NAS",
                    host = "nasmazhuo.me",
                    port = 22,
                    username = "mazhuo",
                    passwordEncrypted = "Maboji@97", // 实际开发时记得加密
                    protocolType = ProtocolType.SFTP, // 👈 完美支持你的枚举
                    rootPath = "/"
                )
            )
        }
    }
}