package com.example.mygoxavemujica_music_game

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.graphics.BitmapFactory
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import com.example.mygoxavemujica_music_game.database.MyDatabaseHelper
import com.example.mygoxavemujica_music_game.model.Song
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat


class SongListView2 : AppCompatActivity() {
    private lateinit var image: ImageView
    private lateinit var songName: TextView
    private lateinit var singerName: TextView
    private lateinit var pointText: TextView
    private lateinit var imgLevel:ImageView
    private lateinit var dbHelper: MyDatabaseHelper
    private val songList = mutableListOf<Song>()
    private var currentIndex = 0

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler()
    private var loopRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_list_view2)

        // 設定全螢幕沉浸式模式
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val startButton = findViewById<ImageView>(R.id.start)
        image = findViewById(R.id.image)
        songName = findViewById(R.id.songName)
        singerName = findViewById(R.id.singerName)
        pointText = findViewById(R.id.pointText)
        imgLevel = findViewById(R.id.imgLevel)
        dbHelper = MyDatabaseHelper(this)

        loadSongsFromDatabase()
        updateUIWithAnimation(0)

        startButton.setOnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
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
                            currentIndex = (currentIndex + 1) % songList.size
                            updateUIWithAnimation(1)
                        } else if (endY - startY > 50) {
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
                cursor.getString(7),
                cursor.getString(5)
            )
            songList.add(song)
        }
        cursor.close()
    }

    // 從 songtime 表讀取段落開始與結束時間(毫秒)
    private fun getSongTimeSegment(songName: String): Pair<Int, Int>? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT start_time, end_time FROM songtime WHERE name = ?", arrayOf(songName))
        var segment: Pair<Int, Int>? = null
        if (cursor.moveToFirst()) {
            val startTime = cursor.getInt(0)
            val endTime = cursor.getInt(1)
            segment = Pair(startTime, endTime)
        }
        cursor.close()
        return segment
    }

    private fun playLoopSegment(resId: Int, startMs: Int, endMs: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, resId).apply {
            seekTo(startMs)
            start()
        }

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

    private fun updateLevelImage(pointScore: Int, imageView: ImageView) {
        if (pointScore == 0) {
            imageView.setImageDrawable(null)  // 清除圖片
            return
        }
        when {
            pointScore > 960000 -> imageView.setImageResource(R.drawable.s)
            pointScore > 820000 -> imageView.setImageResource(R.drawable.a)
            pointScore > 680000 -> imageView.setImageResource(R.drawable.b)
            pointScore > 540000 -> imageView.setImageResource(R.drawable.c)
            else -> imageView.setImageResource(R.drawable.fail)
        }
    }

    private fun updateUIWithAnimation(direction: Int) {
        val song = songList[currentIndex]

        pointText.text = "Score: ${song.point.padStart(7, '0')}"
        val resId = resources.getIdentifier(song.img, "drawable", packageName)
        val musicResId = resources.getIdentifier(song.musicResName, "raw", packageName)

        val pointScoreInt = song.point.toIntOrNull() ?: 0
        updateLevelImage(pointScoreInt, imgLevel)  // 用 imgLevel 更新等級圖示

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blurImageView = findViewById<ImageView>(R.id.background)
            val bitmap = BitmapFactory.decodeResource(resources, resId)
            val renderEffect = RenderEffect.createBlurEffect(40f, 40f, Shader.TileMode.CLAMP)
            blurImageView.setRenderEffect(renderEffect)
            blurImageView.setImageBitmap(bitmap)
            blurImageView.visibility = View.VISIBLE
        }

        if (direction == 0) {
            image.setImageResource(resId)
            songName.text = song.name
            singerName.text = "Singer：" + song.singer

            val segment = getSongTimeSegment(song.name)
            if (segment != null) {
                playLoopSegment(musicResId, segment.first, segment.second)
            } else {
                playLoopSegment(musicResId, 0, 30000)
            }
            return
        }

        val outFromY = if (direction > 0) -300f else 300f
        val inFromY = -outFromY

        image.animate().translationY(outFromY).alpha(0f).setDuration(150).withEndAction {
            image.setImageResource(resId)
            image.translationY = inFromY
            image.animate().translationY(0f).alpha(1f).setDuration(150).start()

            songName.text = song.name
            singerName.text = "Singer：" + song.singer

            val segment = getSongTimeSegment(song.name)
            if (segment != null) {
                playLoopSegment(musicResId, segment.first, segment.second)
            } else {
                playLoopSegment(musicResId, 0, 30000)
            }
        }.start()

        songName.animate().alpha(0f).setDuration(100).withEndAction {
            songName.animate().alpha(1f).setDuration(100).start()
        }.start()

        singerName.animate().alpha(0f).setDuration(100).withEndAction {
            singerName.animate().alpha(1f).setDuration(100).start()
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        loopRunnable?.let { handler.removeCallbacks(it) }
    }
}
