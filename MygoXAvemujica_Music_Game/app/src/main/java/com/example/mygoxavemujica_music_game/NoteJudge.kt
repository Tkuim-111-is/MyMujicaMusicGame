package com.example.mygoxavemujica_music_game

import android.graphics.Bitmap
import android.graphics.RectF

class NoteJudge(
    private val noteImage: Bitmap,
    private val laneLineCount: Int,
    private val zMax: Float
) {
    fun isNoteTouched(note: Note, x: Float, y: Float, now: Long, lineStartX: FloatArray, lineEndX: FloatArray, lineEndY: FloatArray, viewHeight: Int): Boolean {
        val i = note.lane
        if (i < 0 || i >= laneLineCount - 1) return false
        val z = (note.time - now).toFloat()
        if (z > zMax || z < -300) return false
        val t = z / zMax
        val cx = (lerp(lineStartX[i], lineEndX[i], t) + lerp(lineStartX[i + 1], lineEndX[i + 1], t)) / 2
        val cy = lerp(viewHeight - 200f, lineEndY[i], t)
        val scale = (1f / (1f + z * 0.003f)).coerceAtMost(0.75f)
        val width = noteImage.width * scale
        val height = noteImage.height * scale
        return RectF(cx - width / 2, cy - height / 2, cx + width / 2, cy + height / 2).contains(x, y)
    }

    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start * (1 - t) + end * t
    }
}