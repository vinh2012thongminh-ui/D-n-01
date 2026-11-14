package com.vinhho.onthi10ai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        LocalUserEntity::class,
        TutorHistoryEntity::class,
        ExamResultEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OnThiDatabase : RoomDatabase() {
    abstract fun onThiDao(): OnThiDao

    companion object {
        @Volatile
        private var instance: OnThiDatabase? = null

        fun getInstance(context: Context): OnThiDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    OnThiDatabase::class.java,
                    "onthi10_local.db"
                ).build().also { instance = it }
            }
        }
    }
}
