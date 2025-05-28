package com.example.mygoxavemujica_music_game.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDatabaseHelper(context: Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "MyAppDB.db"
        private const val DATABASE_VERSION = 3
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUserTable = """
            CREATE TABLE songlist (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                singer TEXT NOT NULL,
                BPM INTEGER NOT NULL,
                IMG TEXT NOT NULL,
                musicResName TEXT NOT NULL,
                jsonFileName TEXT NOT NULL
            );
        """.trimIndent()

        db.execSQL(createUserTable)

        db.execSQL("INSERT INTO songlist (name, singer, BPM, IMG, musicResName, jsonFileName) VALUES ('影色舞', 'MyGO!!!!!', 166,'img_silhouettedance', 'music_silhouettedance', 'silhouettedance.json')")
        db.execSQL("INSERT INTO songlist (name, singer, BPM, IMG, musicResName, jsonFileName) VALUES ('Imprisoned XII', 'Ave Mujica', 158,'img_imprisonedxii', 'music_imprisonedxii', 'imprisonedxii.json')")
        db.execSQL("INSERT INTO songlist (name, singer, BPM, IMG, musicResName, jsonFileName) VALUES ('KiLLKiSS', 'Ave Mujica', 200,'img_killkiss', 'music_killkiss', 'killkiss.json')")
        db.execSQL("INSERT INTO songlist (name, singer, BPM, IMG, musicResName, jsonFileName) VALUES ('春日影', 'MyGO!!!!!', 97,'img_haruhikage', 'music_haruhikage', 'haruhikage.json')")
        db.execSQL("INSERT INTO songlist (name, singer, BPM, IMG, musicResName, jsonFileName) VALUES ('迷星叫', 'MyGO!!!!!', 190,'img_mayoiuta', 'music_mayoiuta', 'mayoiuta.json')")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS songlist")
        onCreate(db)
    }
}
