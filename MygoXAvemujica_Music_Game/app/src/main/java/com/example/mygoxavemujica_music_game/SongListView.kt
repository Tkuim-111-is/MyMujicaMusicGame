package com.example.mygoxavemujica_music_game

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mygoxavemujica_music_game.adapter.SongListAdapter
import com.example.mygoxavemujica_music_game.database.MyDatabaseHelper
import com.example.mygoxavemujica_music_game.model.Song

class SongListView : AppCompatActivity() {

    private lateinit var dbHelper: MyDatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongListAdapter
    private val songList = mutableListOf<Song>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_list_view)

        dbHelper = MyDatabaseHelper(this)

        loadSongsFromDatabase()

        recyclerView = findViewById(R.id.recyclerView)
        val songImage = findViewById<ImageView>(R.id.song_image)
        val detailTitle = findViewById<TextView>(R.id.detail_title)
        val detailSinger = findViewById<TextView>(R.id.detail_singer)
        val startButton = findViewById<Button>(R.id.start_button)

        val onItemClick: (Song) -> Unit = { selectedSong ->
            detailTitle.text = "歌名：${selectedSong.name}"
            detailSinger.text = "演唱：${selectedSong.singer}"
            val resId = resources.getIdentifier(selectedSong.img, "drawable", packageName)

            if (resId != 0) {
                songImage.setImageResource(resId)
            } else {
                // 找不到圖片時，設定預設圖片或隱藏 ImageView
                songImage.setImageResource(R.drawable.img_killkiss)
            }

            startButton.setOnClickListener {
                val intent = Intent(this, musicgame1::class.java)
                intent.putExtra("songTitle", selectedSong.name)
                startActivity(intent)
            }
        }

        adapter = SongListAdapter(songList, onItemClick)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        recyclerView.addItemDecoration(WhiteDividerItemDecoration(1))

        if (songList.isNotEmpty()) {
            onItemClick(songList[0])
        }
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
}
