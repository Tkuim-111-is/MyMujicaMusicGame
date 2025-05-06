package com.example.mygoxavemujica_music_game.database


import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDatabaseHelper(context: Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "MyAppDB.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUserTable = """
            CREATE TABLE songlist (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                singer TEXT NOT NULL,
                BPM INTEGER NOT NULL
            );
        """.trimIndent()

        db.execSQL(createUserTable)

        db.execSQL("INSERT INTO songlist (name, singer, BPM) VALUES ('A', 'Alice', 12)")
        db.execSQL("INSERT INTO songlist (name, singer, BPM) VALUES ('B', 'Bob', 23)")
        db.execSQL("INSERT INTO songlist (name, singer, BPM) VALUES ('C', 'Charlie', 34)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }
}
