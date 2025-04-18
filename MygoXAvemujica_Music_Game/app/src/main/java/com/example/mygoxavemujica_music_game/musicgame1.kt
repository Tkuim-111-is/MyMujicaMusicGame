package com.example.mygoxavemujica_music_game

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity

class musicgame1 : AppCompatActivity() {
    private lateinit var noteView: NoteView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notes = loadNotesFromJson()
        noteView = NoteView(this, notes)

        noteView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                noteView.handleTouch(event.x, event.y)
            }
            true
        }

        setContentView(noteView) // ⚠️ 這裡會蓋掉原本的 XML 畫面
    }

    private fun loadNotesFromJson(): List<Note> {
        val json = assets.open("musicgame1.json").bufferedReader().use { it.readText() }
        val notes = mutableListOf<Note>()
        val jsonObject = org.json.JSONObject(json)
        val notesArray = jsonObject.getJSONArray("notes")
        for (i in 0 until notesArray.length()) {
            val obj = notesArray.getJSONObject(i)
            notes.add(Note(obj.getLong("time"), obj.getInt("lane")))
        }
        return notes
    }
}


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

