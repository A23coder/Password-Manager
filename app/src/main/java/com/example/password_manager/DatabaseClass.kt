package com.example.password_manager

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DataClass::class], version = 1)
abstract class DatabaseClass :RoomDatabase(){
    abstract fun DataDao(): DataDAO

    companion object {
        @Volatile
        private var INSTANCE: DatabaseClass? = null
        fun getDatabase(context: Context): DatabaseClass {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext ,
                    DatabaseClass::class.java ,
                    "App_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}