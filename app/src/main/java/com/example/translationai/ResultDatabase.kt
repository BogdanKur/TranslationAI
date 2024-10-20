package com.example.translationai

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ResultOfTranslate::class], version = 1, exportSchema = false)
abstract class ResultDatabase: RoomDatabase() {
    abstract val dao: Dao

    companion object{
        @Volatile
        private var INSTANCE: ResultDatabase? = null
        fun getInstance(context: Context): ResultDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if(instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ResultDatabase::class.java,
                        "result_database"
                    ).build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}