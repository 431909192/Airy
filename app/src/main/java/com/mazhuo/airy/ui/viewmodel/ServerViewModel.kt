package com.mazhuo.airy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mazhuo.airy.data.local.dao.ServerConfigDao
import com.mazhuo.airy.data.local.entity.ServerConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServerViewModel @Inject constructor(
    private val serverConfigDao: ServerConfigDao
) : ViewModel() {

    val serverList = serverConfigDao.getAllServers().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addServer(server: ServerConfig) {
        viewModelScope.launch {
            serverConfigDao.insertServer(server)
        }
    }

    fun deleteServer(server: ServerConfig) {
        viewModelScope.launch {
            serverConfigDao.deleteServer(server)
        }
    }
}