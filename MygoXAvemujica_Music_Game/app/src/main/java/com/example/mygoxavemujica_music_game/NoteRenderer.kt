package com.example.mygoxavemujica_music_game

import android.graphics.*
import kotlin.math.*

class NoteRenderer(
    private val noteImage1: Bitmap,
    private val noteImage2: Bitmap,
    private val noteImage3: Bitmap,
    private val laneLineCount: Int,
    private val zMax: Float
) {
    fun drawAllNotes(
        canvas: Canvas,
        notes: List<Note>,
        now: Long,
        judgeY: Float,
        lineStartX: FloatArray,
        lineEndX: FloatArray,
        lineEndY: FloatArray,
        paint: Paint,
        isHolding: Boolean,
        holdingNote: Note?,
        holdEndNote: Note?,
        currentHoldProgressTime: Long
    ) {
        for (note in notes) {
            if (!note.hit) {
                when (note.type) {
                    1.0 -> drawTap(canvas, note, now, judgeY, lineStartX, lineEndX, lineEndY, paint)
                    2.0 -> {
                        val endNote = notes.find { it.type == 2.5 && it.lane == note.lane && it.time > note.time && !it.hit }
                        if (endNote != null) {
                            drawHold(canvas, note, endNote, now, judgeY, lineStartX, lineEndX, lineEndY, paint, isHolding, holdingNote, holdEndNote, currentHoldProgressTime)
                        }
                    }
                    3.0 -> drawFlick(canvas, note, now, judgeY, lineStartX, lineEndX, lineEndY, paint)
                }
            }
        }
    }

    private fun drawTap(canvas: Canvas, note: Note, now: Long, judgeY: Float, lineStartX: FloatArray, lineEndX: FloatArray, lineEndY: FloatArray, paint: Paint) {
        drawNoteImage(canvas, note, noteImage1, now, judgeY, lineStartX, lineEndX, lineEndY, paint)
    }

    private fun drawFlick(canvas: Canvas, note: Note, now: Long, judgeY: Float, lineStartX: FloatArray, lineEndX: FloatArray, lineEndY: FloatArray, paint: Paint) {
        drawNoteImage(canvas, note, noteImage3, now, judgeY, lineStartX, lineEndX, lineEndY, paint)
    }

    private fun drawNoteImage(canvas: Canvas, note: Note, image: Bitmap, now: Long, judgeY: Float, lineStartX: FloatArray, lineEndX: FloatArray, lineEndY: FloatArray, paint: Paint) {
        val i = note.lane
        if (i < 0 || i >= laneLineCount - 1) return
        val z = (note.time - now)
        if (z > zMax) return
        val t = z / zMax
        val leftX = lerp(lineStartX[i], lineEndX[i], t)
        val rightX = lerp(lineStartX[i + 1], lineEndX[i + 1], t)
        val x = (leftX + rightX) / 2f
        val y = lerp(judgeY, lineEndY[i], t)

        val rawScale = 1f / (1f + z * 0.003f)
        val scale = rawScale.coerceAtMost(0.75f)
        val newWidth = image.width * scale
        val newHeight = image.height * scale

        val rect = RectF(
            x - newWidth / 2f,
            y - newHeight / 2f,
            x + newWidth / 2f,
            y + newHeight / 2f
        )

        canvas.drawBitmap(image, null, rect, paint)
    }

    private fun drawHold(
        canvas: Canvas,
        startNote: Note,
        endNote: Note,
        now: Long,
        judgeY: Float,
        lineStartX: FloatArray,
        lineEndX: FloatArray,
        lineEndY: FloatArray,
        paint: Paint,
        isHolding: Boolean,
        holdingNote: Note?,
        holdEndNote: Note?,
        currentHoldProgressTime: Long
    ) {
        val i = startNote.lane
        if (i < 0 || i >= laneLineCount - 1) return

        val zStart = if (isHolding  && holdingNote == startNote && holdEndNote == endNote) 0f else (startNote.time - now).toFloat()
        val tStart = zStart / zMax

        val endTimeDynamic = endNote.time
        val zEnd = (endTimeDynamic - now).toFloat()
        val tEnd = zEnd / zMax

        val shrink = 20f
        val xLeftStart = lerp(lineStartX[i], lineEndX[i], tStart) + shrink
        val xRightStart = lerp(lineStartX[i + 1], lineEndX[i + 1], tStart) - shrink
        val xLeftEnd = lerp(lineStartX[i], lineEndX[i], tEnd) + shrink
        val xRightEnd = lerp(lineStartX[i + 1], lineEndX[i + 1], tEnd) - shrink

        val yStart = if (isHolding && holdingNote == startNote && holdEndNote == endNote)
            judgeY // 按下後起點固定在判定線
        else
            lerp(judgeY, lineEndY[i], tStart) // 未按下前正常往下掉
        val yEnd = lerp(judgeY, lineEndY[i], tEnd)

        val path = Path().apply {
            moveTo(xLeftStart, yStart)
            lineTo(xRightStart, yStart)
            lineTo(xRightEnd, yEnd)
            lineTo(xLeftEnd, yEnd)
            close()
        }

        paint.color = Color.argb(150, 0, 255, 0)
        canvas.drawPath(path, paint)

        if (abs(xLeftStart - xLeftEnd) < 1f && abs(yStart - yEnd) < 1f) {
            startNote.hit = true
            endNote.hit = true
            return
        }

        // 畫起點
        if (!startNote.hit && !(isHolding && holdingNote == startNote && holdEndNote == endNote)) {
            drawNoteImage(canvas, startNote, noteImage2, now, judgeY, lineStartX, lineEndX, lineEndY, paint)
        }
    }

    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start * (1 - t) + end * t
    }
}