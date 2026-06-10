package com.mazhuo.airy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mazhuo.airy.data.local.entity.ServerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    // 使用 Flow 实时监听数据库变化
    @Query("SELECT * FROM remote_servers ORDER BY id DESC")
    fun getAllServersFlow(): Flow<List<ServerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: ServerEntity)

    @Query("DELETE FROM remote_servers WHERE id = :serverId")
    suspend fun deleteServer(serverId: Long)
}