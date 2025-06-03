package com.example.mygoxavemujica_music_game

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler()
    private var loopRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val startBtn = findViewById<Button>(R.id.start_game)
        startBtn.setOnClickListener {
            val intent = Intent(this, SongListView2::class.java)
            startActivity(intent)
        }

        val resId = resources.getIdentifier("the_first_take", "raw", packageName)
        val startMs = 25000      // 25秒
        val endMs = 285000       // 4分45秒

        playLoopSegment(resId, startMs, endMs)
    }

    private fun playLoopSegment(resId: Int, startMs: Int, endMs: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, resId)
        mediaPlayer?.seekTo(startMs)
        mediaPlayer?.start()

        loopRunnable?.let { handler.removeCallbacks(it) }
        loopRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let { mp ->
                    if (mp.currentPosition >= endMs) {
                        mp.seekTo(startMs)
                    }
                }
                handler.postDelayed(this, 100)
            }
        }
        handler.post(loopRunnable!!)
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        loopRunnable?.let { handler.removeCallbacks(it) }
    }
}
