package com.mazhuo.airy.ui.home.model

import com.mazhuo.airy.data.local.entity.ServerConfig
sealed class HomeItem {
    // 1. 本地存储静态项
    object LocalStorage : HomeItem()

    // 2. 动态服务器项（包装了 Room 实体）
    data class SavedServer(val config : ServerConfig) : HomeItem()

    // 3. 新增远程连接动作项
    object AddConnection : HomeItem()
}