package com.example.mygoxavemujica_music_game

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.FileOutputStream

object ReadOnlySongDB {
    private  const val DBNAME = "Songs.db"
    private fun copyDatabaseFromAssets(context: Context) {
        val dbPath = context.getDatabasePath(DBNAME)
        if (!dbPath.exists()) {
            dbPath.parentFile?.mkdirs()
            context.assets.open(DBNAME).use { input ->
                FileOutputStream(dbPath).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    fun logAllSongs(context: Context) {

        copyDatabaseFromAssets(context)

        val dbPath = context.getDatabasePath(DBNAME).path
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

        val cursor = db.query("Details", null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow("TITLE"))
            val artist = cursor.getString(cursor.getColumnIndexOrThrow("artist"))
            val duration = cursor.getString(cursor.getColumnIndexOrThrow("duration"))
            val bpm = cursor.getString(cursor.getColumnIndexOrThrow("bpm"))

            Log.d("ReadOnlyDB", "歌名: $title 歌手: $artist 歌曲長度: $duration BPM: $bpm")
        }
        cursor.close()
        db.close()
    }
}