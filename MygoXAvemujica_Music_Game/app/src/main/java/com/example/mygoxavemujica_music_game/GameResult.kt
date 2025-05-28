package com.example.mygoxavemujica_music_game

object GameResult {
    var songTitle: String = ""
    var perfectCount = 0
    var greatCount = 0
    var goodCount = 0
    var badCount = 0
    var missCount = 0
    var maxCombo = 0

    fun reset() {
        songTitle = ""
        perfectCount = 0
        greatCount = 0
        goodCount = 0
        badCount = 0
        missCount = 0
        maxCombo = 0
    }
}