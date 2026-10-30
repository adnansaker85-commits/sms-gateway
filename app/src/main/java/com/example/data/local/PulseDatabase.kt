package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ThreadMetaEntity::class,
        StarredMessageEntity::class,
        ScheduledMessageEntity::class,
        BlockedNumberEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PulseDatabase : RoomDatabase() {
    abstract fun threadMetaDao(): ThreadMetaDao
    abstract fun starredMessageDao(): StarredMessageDao
    abstract fun scheduledMessageDao(): ScheduledMessageDao
    abstract fun blockedNumberDao(): BlockedNumberDao

    companion object {
        @Volatile
        private var INSTANCE: PulseDatabase? = null

        fun getInstance(context: Context): PulseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PulseDatabase::class.java,
                    "pulse_messenger.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
