package com.example.mygoxavemujica_music_game

data class Note(
    val time: Long,
    val lane: Int,
    val type: Double,
    var y: Float = 0f,
    var hit: Boolean = false,
    var isHolding: Boolean = false,
    var isFlicked: Boolean = false
)