package com.example.persistenttimerapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.persistenttimerapp.data.entities.Category
import com.example.persistenttimerapp.data.entities.Task

@Database(entities = [Category::class, Task::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
