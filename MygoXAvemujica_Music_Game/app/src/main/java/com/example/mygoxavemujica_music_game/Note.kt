package com.example.mygoxavemujica_music_game

data class Note(
    val time: Long,
    val lane: Int,
    var y: Float = 0f,
    var hit: Boolean = false
)