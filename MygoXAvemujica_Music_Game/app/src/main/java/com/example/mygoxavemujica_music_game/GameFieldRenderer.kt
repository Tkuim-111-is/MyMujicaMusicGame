package com.example.mygoxavemujica_music_game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class GameFieldRenderer(
    private val laneLineCount: Int,
    private val zMax: Float
) {
    val lineStartX = FloatArray(laneLineCount)
    val lineStartY = FloatArray(laneLineCount)
    val lineEndX = FloatArray(laneLineCount)
    val lineEndY = FloatArray(laneLineCount)

    fun drawField(canvas: Canvas, paint: Paint, width: Int, height: Int) {
        val centerX = width / 2f
        val judgeY = height - 200f
        val laneSpacing = 250f
        val perspectiveFactor = 0.1f

        canvas.drawColor(Color.BLACK)

        paint.strokeWidth = 4f
        paint.color = Color.DKGRAY

        for (i in 0 until laneLineCount) {
            val offset = (i - (laneLineCount - 1) / 2f) * laneSpacing
            val xStart = centerX + offset
            val xEnd = centerX + offset * perspectiveFactor
            val yStart = judgeY
            val yEnd = judgeY - zMax * 1.2f

            lineStartX[i] = xStart
            lineStartY[i] = yStart
            lineEndX[i] = xEnd
            lineEndY[i] = yEnd

            canvas.drawLine(xStart, yStart, xEnd, yEnd, paint)
        }

        // 畫判定線
        paint.color = Color.RED
        paint.strokeWidth = 20f
        canvas.drawLine(0f, judgeY, width.toFloat(), judgeY, paint)
    }
}