package com.example.hisabbook.data.local

import android.content.Context

object DatabaseProvider {
    private var database: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val instance = AppDatabase.getInstance(context)
            database = instance
            instance
        }
    }
}
