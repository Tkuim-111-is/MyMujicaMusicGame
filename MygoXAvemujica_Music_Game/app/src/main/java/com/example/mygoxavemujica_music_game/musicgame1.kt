package com.example.mygoxavemujica_music_game

import android.database.Cursor
import android.media.MediaPlayer
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.example.mygoxavemujica_music_game.database.MyDatabaseHelper
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.example.mygoxavemujica_music_game.model.FinalViewActivity

class musicgame1 : AppCompatActivity() {
    private lateinit var dbHelper: MyDatabaseHelper
    private lateinit var noteView: NoteView
    private lateinit var mediaPlayer: MediaPlayer
    init {
        GameResult.reset()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val songTitle = intent.getStringExtra("songTitle") ?: error("Missing songTitle")
        music.songTitle = songTitle

        dbHelper = MyDatabaseHelper(this)
        val db = dbHelper.readableDatabase
        // 只查詢指定歌曲
        val cursor = db.rawQuery("SELECT * FROM songlist WHERE name = ?", arrayOf(songTitle))
        if (!cursor.moveToFirst()) {
            cursor.close()
            error("找不到歌曲：$songTitle")
        }

        // 取得資料庫欄位
        val singer = cursor.getString(cursor.getColumnIndexOrThrow("singer"))
        val bpm = cursor.getInt(cursor.getColumnIndexOrThrow("BPM"))
        val img = cursor.getString(cursor.getColumnIndexOrThrow("IMG"))
        val musicResName = cursor.getString(cursor.getColumnIndexOrThrow("musicResName"))
        val jsonFileName = cursor.getString(cursor.getColumnIndexOrThrow("jsonFileName"))
        val musicResId = resources.getIdentifier(musicResName, "raw", packageName)

        val notes = loadNotesFromJson(jsonFileName)
        noteView = NoteView(this, notes)
        val musicStartTime = System.currentTimeMillis()
        noteView.setStartTime(musicStartTime)

        mediaPlayer = MediaPlayer.create(this, musicResId)
        mediaPlayer.start()  // 播放音樂

        mediaPlayer.setOnCompletionListener {
            // 音樂播放完後延遲 2 秒執行轉場
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, FinalViewActivity::class.java)
                startActivity(intent)
                finish()
            }, 2000)
        }

        setContentView(noteView) //蓋掉原本的 XML 畫面
        cursor.close()
    }

    // 停止音樂（例如當遊戲結束時）
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()  // 停止播放
        mediaPlayer.release()  // 釋放資源
    }

    private fun loadNotesFromJson(jsonFileName: String): List<Note> {
        val json = assets.open(jsonFileName).bufferedReader().use { it.readText() }
        val notes = mutableListOf<Note>()
        val jsonObject = org.json.JSONObject(json)
        val notesArray = jsonObject.getJSONArray("notes")
        for (i in 0 until notesArray.length()) {
            val obj = notesArray.getJSONObject(i)
            notes.add(Note(obj.getLong("time"), obj.getInt("lane"), obj.getDouble("type")))
        }
        return notes
    }
}

/*
// 根據歌曲名稱決定資源檔名（這裡假設 IMG 欄位與資源檔名有關）
        val musicResId = when (songTitle) {
            "影色舞" -> R.raw.music_silhouettedance
            "Imprisoned XII" -> R.raw.music_imprisonedxii
            "KiLLKiSS" -> R.raw.music_killkiss
            "春日影" -> R.raw.music_haruhikage
            "迷星叫" -> R.raw.music_mayoiuta
            else -> R.raw.music_silhouettedance // 預設或測試用
        }

        val jsonFileName = when (songTitle) {
            "影色舞" -> "silhouettedance.json"
            "Imprisoned XII" -> "imprisonedxii.json"
            "KiLLKiSS" -> "killkiss.json"
            "春日影" -> "haruhikage.json"
            "迷星叫" -> "mayoiuta.json"
            else -> "silhouettedance.json"
        }

// 停止音樂（例如當遊戲結束時）
override fun onDestroy() {
    super.onDestroy()
    mediaPlayer.stop()  // 停止播放
    mediaPlayer.release()  // 釋放資源
}
*/

/*package com.example.mygoxavemujica_music_game

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.TextureView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class musicgame1 : AppCompatActivity() {
    private lateinit var textureView: TextureView
    private lateinit var glSurfaceView: GLSurfaceView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_musicgame1)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}*/

