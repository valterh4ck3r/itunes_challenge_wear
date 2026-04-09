package com.valternegreiros.itunes_challenge_wear.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.valternegreiros.itunes_challenge_wear.data.local.dao.SongDao
import com.valternegreiros.itunes_challenge_wear.data.local.entity.SongEntity

@Database(
    entities = [SongEntity::class],
    version = 3,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getInstance(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "music_ai_db"
                )
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
        }
    }
}
