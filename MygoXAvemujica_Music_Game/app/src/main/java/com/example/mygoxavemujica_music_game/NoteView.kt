package com.example.mygoxavemujica_music_game

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View

class NoteView(context: Context, private val notes: List<Note>) : View(context) {

    private val paint = Paint()
    private var startTime = System.currentTimeMillis()
    private val speed = 1.0f
    private val laneLineCount = 8
    private val zMax = 1000f

    private val noteImage1 = BitmapFactory.decodeResource(resources, R.raw.circle)
    private val noteImage2 = BitmapFactory.decodeResource(resources, R.raw.longtopras)
    private val noteImage3 = BitmapFactory.decodeResource(resources, R.raw.jump)

    // 模組化功能
    private val fieldRenderer = GameFieldRenderer(laneLineCount, zMax)
    private val noteRenderer = NoteRenderer(noteImage1, noteImage2, noteImage3, laneLineCount, zMax)
    private val judge = NoteJudge(noteImage1, laneLineCount, zMax)

    // hold 判定狀態
    private var holdingNote: Note? = null
    private var holdEndNote: Note? = null
    private var isHolding = false
    private var currentHoldProgressTime: Long = 0L

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = System.currentTimeMillis() - startTime
        val judgeY = height - 200f

        // 繪製背景與判定線
        fieldRenderer.drawField(canvas, paint, width, height)

        // 繪製音符
        noteRenderer.drawAllNotes(
            canvas = canvas,
            notes = notes,
            now = now,
            judgeY = judgeY,
            lineStartX = fieldRenderer.lineStartX,
            lineEndX = fieldRenderer.lineEndX,
            lineEndY = fieldRenderer.lineEndY,
            paint = paint,
            isHolding = isHolding,
            holdingNote = holdingNote,
            holdEndNote = holdEndNote,
            currentHoldProgressTime = currentHoldProgressTime
        )

        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val now = System.currentTimeMillis() - startTime

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 判斷是否為長按起點
                notes.find {
                    it.type == 2.0 && !it.hit && judge.isNoteTouched(it, x, y, now, fieldRenderer.lineStartX, fieldRenderer.lineEndX, fieldRenderer.lineEndY, height)
                }?.let { start ->
                    val end = notes.find { it.type == 2.5 && it.lane == start.lane && it.time > start.time && !it.hit }
                    if (end != null) {
                        //開始追蹤長按，但不設 hit
                        holdingNote = start
                        holdEndNote = end
                        isHolding = true
                        currentHoldProgressTime = start.time
                        invalidate()
                        return true
                    }
                }

                // 單擊音符
                notes.find { it.type == 1.0 && !it.hit && judge.isNoteTouched(it, x, y, now, fieldRenderer.lineStartX, fieldRenderer.lineEndX, fieldRenderer.lineEndY, height) }?.let {
                    it.hit = true
                    invalidate()
                    return true
                }

                // 上滑音符
                notes.find { it.type == 3.0 && !it.hit && judge.isNoteTouched(it, x, y, now, fieldRenderer.lineStartX, fieldRenderer.lineEndX, fieldRenderer.lineEndY, height) }?.let {
                    it.hit = true
                    invalidate()
                    return true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isHolding && holdingNote != null && holdEndNote != null) {
                    if (isTouchInHoldRect(x, y, now)) {
                        currentHoldProgressTime = now.coerceAtMost(holdEndNote!!.time)
                        invalidate()
                    } else {
                        cancelHold()
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                holdingNote?.isHolding = false
                holdEndNote?.isHolding = false

                isHolding = false
                holdingNote = null
                holdEndNote = null
                invalidate()
            }
        }

        return true
    }

    private fun isTouchInHoldRect(x: Float, y: Float, now: Long): Boolean {
        val startNote = holdingNote ?: return false
        val endNote = holdEndNote ?: return false
        val i = startNote.lane
        if (i < 0 || i >= laneLineCount - 1) return false

        val zStart = 0f
        val tStart = zStart / zMax
        val zEnd = (currentHoldProgressTime - now).toFloat()
        val tEnd = zEnd / zMax

        val shrink = 20f
        val xLeftStart = lerp(fieldRenderer.lineStartX[i], fieldRenderer.lineEndX[i], tStart) + shrink
        val xRightStart = lerp(fieldRenderer.lineStartX[i + 1], fieldRenderer.lineEndX[i + 1], tStart) - shrink
        val xLeftEnd = lerp(fieldRenderer.lineStartX[i], fieldRenderer.lineEndX[i], tEnd) + shrink
        val xRightEnd = lerp(fieldRenderer.lineStartX[i + 1], fieldRenderer.lineEndX[i + 1], tEnd) - shrink

        val yStart = lerp(height - 200f, fieldRenderer.lineEndY[i], tStart)
        val yEnd = lerp(height - 200f, fieldRenderer.lineEndY[i], tEnd)

        val minX = minOf(xLeftStart, xRightStart, xLeftEnd, xRightEnd)
        val maxX = maxOf(xLeftStart, xRightStart, xLeftEnd, xRightEnd)
        val minY = minOf(yStart, yEnd)
        val maxY = maxOf(yStart, yEnd)

        return x in minX..maxX && y in minY..maxY
    }

    private fun cancelHold() {
        isHolding = false
        holdingNote = null
        holdEndNote = null
        invalidate()
    }

    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start * (1 - t) + end * t
    }

    fun setStartTime(time: Long) {
        startTime = time
    }
}