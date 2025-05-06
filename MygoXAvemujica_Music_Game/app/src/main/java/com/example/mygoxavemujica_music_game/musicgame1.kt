package com.example.mygoxavemujica_music_game

import android.media.MediaPlayer
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity

class musicgame1 : AppCompatActivity() {
    private lateinit var noteView: NoteView
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notes = loadNotesFromJson()
        noteView = NoteView(this, notes)

        mediaPlayer = MediaPlayer.create(this, R.raw.dora_a_mu)  // 這裡的 "music" 是放在 res/raw 下的音樂檔名
        val musicStartTime = System.currentTimeMillis()
        noteView.setStartTime(musicStartTime)
        mediaPlayer.start()  // 播放音樂
/*
        noteView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                noteView.handleTouch(event.x, event.y)
            }
            true
        }
*/
        setContentView(noteView) //蓋掉原本的 XML 畫面
    }

    // 停止音樂（例如當遊戲結束時）
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()  // 停止播放
        mediaPlayer.release()  // 釋放資源
    }

    private fun loadNotesFromJson(): List<Note> {
        val json = assets.open("musicgame1.json").bufferedReader().use { it.readText() }
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

