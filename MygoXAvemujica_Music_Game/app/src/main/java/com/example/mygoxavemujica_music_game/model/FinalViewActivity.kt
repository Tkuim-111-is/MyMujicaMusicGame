package com.example.mygoxavemujica_music_game.model

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mygoxavemujica_music_game.R
import com.example.mygoxavemujica_music_game.GameResult
import com.example.mygoxavemujica_music_game.music
import com.example.mygoxavemujica_music_game.SongListView2
import com.example.mygoxavemujica_music_game.musicgame1
import com.example.mygoxavemujica_music_game.database.MyDatabaseHelper
import org.w3c.dom.Text
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class FinalViewActivity : AppCompatActivity() {

    private lateinit var dbHelper: MyDatabaseHelper
    private lateinit var point: TextView
    private lateinit var MaxCombo: TextView
    private lateinit var Accuracy: TextView
    private lateinit var Perfect: TextView
    private lateinit var Great: TextView
    private lateinit var Good: TextView
    private lateinit var Bad: TextView
    private lateinit var Miss: TextView
    private lateinit var point_image : ImageView
    private lateinit var playsong_image : ImageView
    private lateinit var newbest : TextView
    private lateinit var originpoint : TextView
    private lateinit var pluspoint : TextView

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.final_view)

        // 設定全螢幕沉浸式模式
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        dbHelper = MyDatabaseHelper(this)

        point = findViewById(R.id.point)
        MaxCombo = findViewById(R.id.maxCombo_num)
        Accuracy = findViewById(R.id.accuracy_num)
        Perfect = findViewById(R.id.perfect_num)
        Great = findViewById(R.id.Great_num)
        Good = findViewById(R.id.Good_num)
        Bad = findViewById(R.id.Bad_num)
        Miss = findViewById(R.id.Miss_num)
        newbest = findViewById(R.id.newbest)
        originpoint = findViewById(R.id.originpoint)
        pluspoint = findViewById(R.id.pluspoint)
        point_image = findViewById(R.id.point_image)
        playsong_image = findViewById(R.id.playsong_image)

        Perfect.text = GameResult.perfectCount.toString()
        Great.text = GameResult.greatCount.toString()
        Good.text = GameResult.goodCount.toString()
        Bad.text = GameResult.badCount.toString()
        Miss.text = GameResult.missCount.toString()
        MaxCombo.text = GameResult.maxCombo.toString()

        val totalNotes = GameResult.perfectCount + GameResult.greatCount + GameResult.goodCount + GameResult.badCount + GameResult.missCount
        val hitNotes = GameResult.perfectCount + GameResult.greatCount + GameResult.goodCount + GameResult.badCount
        val accuracy = if (totalNotes > 0) (hitNotes * 100.0 / totalNotes) else 0.0

        Accuracy.text = String.format("%.2f%%", accuracy)

        val point_average = 1000000 / totalNotes
        val pointScore = GameResult.perfectCount * point_average +
                GameResult.greatCount * 0.75 * point_average +
                GameResult.goodCount * 0.5 * point_average +
                GameResult.badCount * 0.25 * point_average

        val pointScoreInt = pointScore.toInt()  // 取整數
        val pointScoreStr = pointScoreInt.toString().padStart(7, '0')  // 左補0至7位數字串

        point.text = pointScoreStr

        val currentPointInDB = dbHelper.getPointBySongName(music.songTitle)
        val finalPointScoreLong = pointScoreInt.toLong()

        if (finalPointScoreLong > currentPointInDB) {
            dbHelper.updatePointBySongName(music.songTitle, finalPointScoreLong.toString())

            newbest.text = "New Best"
            originpoint.text = currentPointInDB.toString().padStart(7, '0')
            pluspoint.text = "+${finalPointScoreLong - currentPointInDB}"
        } else {
            newbest.text = ""
            originpoint.text = ""
            pluspoint.text = ""
        }

        when {
            pointScoreInt > 960000 -> point_image.setImageResource(R.drawable.s)
            pointScoreInt > 820000 -> point_image.setImageResource(R.drawable.a)
            pointScoreInt > 680000 -> point_image.setImageResource(R.drawable.b)
            pointScoreInt > 540000 -> point_image.setImageResource(R.drawable.c)
            else -> point_image.setImageResource(R.drawable.fail)
        }

        val imgName = getImageNameBySongTitle(music.songTitle)
        if (!imgName.isNullOrEmpty()) {
            val resId = resources.getIdentifier(imgName, "drawable", packageName)
            if (resId != 0) {
                playsong_image.setImageResource(resId)
            }
        }

        // 播放 dora_a_mu.mp3 並設置循環
        val resId = resources.getIdentifier("georgette_me_georgette_you", "raw", packageName)
        mediaPlayer = MediaPlayer.create(this, resId).apply {
            isLooping = true
            start()
        }

        findViewById<ImageView>(R.id.goBack).setOnClickListener {
            val intent = Intent(this, SongListView2::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.playAgain).setOnClickListener {
            val intent = Intent(this, musicgame1::class.java)
            intent.putExtra("songTitle", music.songTitle)
            startActivity(intent)
        }
    }

    private fun getImageNameBySongTitle(songTitle: String): String? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT IMG FROM songlist WHERE name = ?", arrayOf(songTitle))
        var imgName: String? = null
        if (cursor.moveToFirst()) {
            imgName = cursor.getString(0)
        }
        cursor.close()
        return imgName
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
