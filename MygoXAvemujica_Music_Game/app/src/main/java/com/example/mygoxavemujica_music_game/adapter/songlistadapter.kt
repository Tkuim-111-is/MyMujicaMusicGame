package com.example.mygoxavemujica_music_game.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mygoxavemujica_music_game.R
import com.example.mygoxavemujica_music_game.model.Song

class SongListAdapter(
    private val songs: List<Song>,
    private val onItemClick: (Song) -> Unit
) : RecyclerView.Adapter<SongListAdapter.SongViewHolder>() {

    inner class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.song_name)

        init {
            view.setOnClickListener {
                onItemClick(songs[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun getItemCount() = songs.size

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.nameText.text = songs[position].name
    }
}
