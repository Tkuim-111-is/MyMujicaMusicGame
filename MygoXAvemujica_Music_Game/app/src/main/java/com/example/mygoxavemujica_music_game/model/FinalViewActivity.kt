package com.example.mygoxavemujica_music_game.model

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mygoxavemujica_music_game.R
import com.example.mygoxavemujica_music_game.GameResult
import com.example.mygoxavemujica_music_game.music
import com.example.mygoxavemujica_music_game.SongListView
import com.example.mygoxavemujica_music_game.database.MyDatabaseHelper
import com.example.mygoxavemujica_music_game.musicgame1

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.final_view)  // 這邊對應你的 XML 檔名
        dbHelper = MyDatabaseHelper(this)

        point = findViewById(R.id.point)
        MaxCombo = findViewById(R.id.maxCombo_num)
        Accuracy = findViewById(R.id.accuracy_num)
        Perfect = findViewById(R.id.perfect_num)
        Great = findViewById(R.id.Great_num)
        Good = findViewById(R.id.Good_num)
        Bad = findViewById(R.id.Bad_num)
        Miss = findViewById(R.id.Miss_num)
        point_image = findViewById(R.id.imageView)
        playsong_image = findViewById(R.id.imageView9)

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

        val point_average = 1000000/totalNotes
        val pointScore = GameResult.perfectCount * point_average + GameResult.greatCount * 0.75 * point_average + GameResult.goodCount * 0.5 * point_average +  GameResult.badCount * 0.25 * point_average
        point.text = pointScore.toString()

        if(pointScore>960000){
            point_image.setImageResource(R.drawable.s)
        }
        else if(pointScore>820000){
            point_image.setImageResource(R.drawable.a)
        }
        else if(pointScore>680000){
            point_image.setImageResource(R.drawable.b)
        }
        else if(pointScore>540000){
            point_image.setImageResource(R.drawable.c)
        }
        else{
            point_image.setImageResource(R.drawable.fail)
        }

        val imgName = getImageNameBySongTitle(music.songTitle)
        if (!imgName.isNullOrEmpty()) {
            val resId = resources.getIdentifier(imgName, "drawable", packageName)
            if (resId != 0) {
                playsong_image.setImageResource(resId)
            }
        }

        val GoBack = findViewById<ImageView>(R.id.goBack)
        GoBack.setOnClickListener {
            val intent = Intent(this, SongListView::class.java)
            startActivity(intent)
        }

        val again = findViewById<ImageView>(R.id.playAgain)
        again.setOnClickListener {
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
}