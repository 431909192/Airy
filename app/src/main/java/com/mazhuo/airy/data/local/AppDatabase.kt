package com.mazhuo.airy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.mazhuo.airy.data.local.dao.ServerConfigDao
import com.mazhuo.airy.data.local.dao.ServerDao
import com.mazhuo.airy.domain.model.ProtocolType
import com.mazhuo.airy.data.local.entity.ServerEntity
import com.mazhuo.airy.data.local.entity.ServerConfig

@Database(
    entities = [ServerEntity::class, ServerConfig::class],
    version = 1,
    exportSchema = false
)
// ⚠️ 极其重要：注册类型转换器，否则 Room 编译到 ProtocolType 枚举时会崩溃
@TypeConverters(AppDatabase.Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // 暴露出供 Hilt 和业务层调用的 Dao 抽象方法
    abstract fun serverDao(): ServerDao
    abstract fun serverConfigDao(): ServerConfigDao

    /**
     * Room 类型转换器
     * 负责在「内存对象」与「SQLite 基础数据类型」之间做双向转换
     */
    class Converters {

        @TypeConverter
        fun fromProtocolType(type: ProtocolType): String {
            return type.name // 将枚举（如 SFTP）转为字符串 "SFTP" 存入数据库
        }

        @TypeConverter
        fun toProtocolType(value: String): ProtocolType {
            return try {
                ProtocolType.valueOf(value) // 从数据库读取字符串 "SFTP" 转回枚举 ProtocolType.SFTP
            } catch (e: IllegalArgumentException) {
                ProtocolType.SFTP // 防御性容错：如果匹配失败，默认回退到 SFTP
            }
        }
    }
}