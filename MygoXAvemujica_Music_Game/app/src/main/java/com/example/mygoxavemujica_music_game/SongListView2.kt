package com.example.mygoxavemujica_music_game

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mygoxavemujica_music_game.database.MyDatabaseHelper
import com.example.mygoxavemujica_music_game.model.Song

class SongListView2 : AppCompatActivity() {
    private lateinit var image: ImageView
    private lateinit var songName: TextView
    private lateinit var singerName: TextView
    private lateinit var pointText: TextView
    private lateinit var dbHelper: MyDatabaseHelper
    private val songList = mutableListOf<Song>()
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_list_view2)

        val startButton = findViewById<ImageView>(R.id.start)
        image = findViewById(R.id.image)
        songName = findViewById(R.id.songName)
        singerName = findViewById(R.id.singerName)
        pointText = findViewById(R.id.pointText)
        dbHelper = MyDatabaseHelper(this)


        loadSongsFromDatabase()
        updateUIWithAnimation(0)

        startButton.setOnClickListener{
            val intent = Intent(this, musicgame1::class.java)
            intent.putExtra("songTitle", songList[currentIndex].name)
            startActivity(intent)
        }

        startButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.alpha = 0.8f
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.alpha = 0.5f
            }
            false
        }


        image.setOnTouchListener(object : View.OnTouchListener {
            var startY = 0f
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> startY = event.y
                    MotionEvent.ACTION_UP -> {
                        val endY = event.y
                        if (startY - endY > 50) {
                            // 向上滑動
                            currentIndex = (currentIndex + 1) % songList.size
                            updateUIWithAnimation(1)
                        } else if (endY - startY > 50) {
                            // 向下滑動
                            currentIndex = (currentIndex - 1 + songList.size) % songList.size
                            updateUIWithAnimation(-1)
                        }
                    }
                }
                return true
            }
        })
    }

    private fun loadSongsFromDatabase() {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM songlist", null)
        while (cursor.moveToNext()) {
            val song = Song(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(4),
                cursor.getString(7)
            )
            songList.add(song)
        }
        cursor.close()
    }

    private fun updateUIWithAnimation(direction: Int) {
        val song = songList[currentIndex]

        pointText.text = "Score: ${song.point.padStart(7, '0')}"

        val resId = resources.getIdentifier(song.img, "drawable", packageName)

        if (direction == 0) {
            // 首次載入：直接設定，不執行動畫
            image.setImageResource(resId)
            songName.text = song.name
            singerName.text = "Singer：" + song.singer
            return
        }

        val outFromY = if (direction > 0) -300f else 300f
        val inFromY = -outFromY

        // 淡出淡入動畫（或可改上下滑入效果）
        image.animate().translationY(outFromY).alpha(0f).setDuration(150).withEndAction {
            image.setImageResource(resId)
            image.translationY = inFromY
            image.animate().translationY(0f).alpha(1f).setDuration(150).start()
        }.start()

        songName.animate().alpha(0f).setDuration(100).withEndAction {
            songName.text = song.name
            songName.animate().alpha(1f).setDuration(100).start()
        }.start()

        singerName.animate().alpha(0f).setDuration(100).withEndAction {
            singerName.text = "Singer：" + song.singer
            singerName.animate().alpha(1f).setDuration(100).start()
        }.start()
    }

}