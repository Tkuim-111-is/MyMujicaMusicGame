package com.example.mygoxavemujica_music_game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import kotlin.math.abs

class NoteView(context: Context, private val notes: List<Note>) : View(context) {
    private val paint = Paint()
    private val startTime = System.currentTimeMillis()
    private val speed = 1.0f
    private val laneLineCount = 8 // 幾條「線」
    private val laneCount = laneLineCount - 1 // 幾個「區間」

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val judgeY = height - 150f
        val now = System.currentTimeMillis() - startTime
        val laneSpacing = 200f
        val zMax = 1000f
        val perspectiveFactor = 0.05f

        canvas.drawColor(Color.BLACK)

        // 計算每條「線」的起點與終點
        val lineStartX = FloatArray(laneLineCount)
        val lineStartY = FloatArray(laneLineCount)
        val lineEndX = FloatArray(laneLineCount)
        val lineEndY = FloatArray(laneLineCount)

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
        paint.strokeWidth = 15f
        canvas.drawLine(0f, judgeY, width.toFloat(), judgeY, paint)

        // 畫音符
        paint.color = Color.BLUE
        for (note in notes) {
            if (!note.hit) {
                val z = (note.time - now) * speed
                if (z < 0 || z > zMax) continue

                val t = z / zMax
                val i = note.lane
                if (i < 0 || i >= laneCount) continue

                val x = (lineStartX[i] * (1 - t) + lineEndX[i] * t +
                        lineStartX[i + 1] * (1 - t) + lineEndX[i + 1] * t) / 2f
                val y = (lineStartY[i] * (1 - t) + lineEndY[i] * t +
                        lineStartY[i + 1] * (1 - t) + lineEndY[i + 1] * t) / 2f

                val scale = 1f / (1f + z * 0.004f)
                val w = 200f * scale
                val h = 40f * scale

                canvas.drawRect(x - w / 2, y - h / 2, x + w / 2, y + h / 2, paint)
            }
        }

        invalidate()
    }

    fun handleTouch(x: Float, y: Float) {
        val now = System.currentTimeMillis() - startTime
        val judgeY = height - 150f
        val laneSpacing = 200f
        val centerX = width / 2f
        val zMax = 1000f
        val perspectiveFactor = 0.05f

        val lineStartX = FloatArray(laneLineCount)
        val lineStartY = FloatArray(laneLineCount)
        val lineEndX = FloatArray(laneLineCount)
        val lineEndY = FloatArray(laneLineCount)

        for (i in 0 until laneLineCount) {
            val offset = (i - (laneLineCount - 1) / 2f) * laneSpacing
            lineStartX[i] = centerX + offset
            lineStartY[i] = judgeY
            lineEndX[i] = centerX + offset * perspectiveFactor
            lineEndY[i] = judgeY - zMax * 1.2f
        }

        for (note in notes) {
            if (!note.hit) {
                val z = (note.time - now) * speed
                if (z < 0 || z > zMax) continue

                val t = z / zMax
                val i = note.lane
                if (i < 0 || i >= laneCount) continue

                val x = (lineStartX[i] * (1 - t) + lineEndX[i] * t +
                        lineStartX[i + 1] * (1 - t) + lineEndX[i + 1] * t) / 2f
                val y = (lineStartY[i] * (1 - t) + lineEndY[i] * t +
                        lineStartY[i + 1] * (1 - t) + lineEndY[i + 1] * t) / 2f

                val scale = 1f / (1f + z * 0.004f)
                val w = 200f * scale
                val h = 40f * scale

                if (
                    abs(y - judgeY) < 60 &&
                    x >= x - w / 2 && x <= x + w / 2
                ) {
                    note.hit = true
                    break
                }
            }
        }
    }
}
