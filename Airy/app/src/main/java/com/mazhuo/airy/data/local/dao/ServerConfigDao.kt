package com.mazhuo.airy.data.local.dao

import androidx.room.*
import com.mazhuo.airy.data.local.entity.ServerConfig
import kotlinx.coroutines.flow.Flow
@Dao
interface ServerConfigDao {
    @Query("SELECT * FROM server_configs ORDER BY lastConnected DESC")
    fun getAllServers(): Flow<List<ServerConfig>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: ServerConfig): Long
    @Update
    suspend fun updateServer(server: ServerConfig)
    @Delete
    suspend fun deleteServer(server: ServerConfig)
    @Query("SELECT * FROM server_configs WHERE id = :id")
    suspend fun getServerById(id: Long): ServerConfig?
}