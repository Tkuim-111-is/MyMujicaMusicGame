package com.example.mygoxavemujica_music_game

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin

class NoteView(context: Context, private val notes: List<Note>) : View(context) {

    private val paint = Paint()
    private var startTime = System.currentTimeMillis()
    private val speed = 1.0f
    private val laneLineCount = 8
    private val zMax = 1000f

    private val noteImage1 = BitmapFactory.decodeResource(resources, R.raw.circle)
    private val noteImage2 = BitmapFactory.decodeResource(resources, R.raw.longtopras)
    private val noteImage3 = BitmapFactory.decodeResource(resources, R.raw.jump)

    private val fieldRenderer = GameFieldRenderer(laneLineCount, zMax)
    private val noteRenderer = NoteRenderer(noteImage1, noteImage2, noteImage3, laneLineCount, zMax)
    private val judge = NoteJudge(noteImage1, laneLineCount, zMax)

    // Hold 判定狀態
    private var holdingNote: Note? = null
    private var holdEndNote: Note? = null
    private var isHolding = false
    private var currentHoldProgressTime: Long = 0L

    // Combo 與動畫
    private var combo = 0
    private var showCombo = false
    private var comboScale = 1.0f
    private var comboBounceTime = 0L

    private val comboPaint = Paint().apply {
        color = Color.WHITE
        textSize = 80f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.RIGHT
        isAntiAlias = true
    }

    // 判定文字顯示相關 (彈跳動畫用)
    private var judgmentText: String? = null
    private var judgmentScale = 1.0f
    private var judgmentBounceStartTime = 0L
    private var judgmentVisible = false

    private val judgmentPaint = Paint().apply {
        color = Color.YELLOW
        textSize = 100f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        setShadowLayer(10f, 0f, 0f, Color.BLACK)
    }

    // 判定時間窗口（毫秒）
    private val PERFECT_WINDOW = 50
    private val GREAT_WINDOW = 100
    private val GOOD_WINDOW = 150
    private val BAD_WINDOW = 200

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = System.currentTimeMillis() - startTime
        val judgeY = height - 200f

        // 背景與判定線
        fieldRenderer.drawField(canvas, paint, width, height)

        // 音符
        noteRenderer.drawAllNotes(
            canvas, notes, now, judgeY,
            fieldRenderer.lineStartX,
            fieldRenderer.lineEndX,
            fieldRenderer.lineEndY,
            paint,
            isHolding,
            holdingNote,
            holdEndNote,
            currentHoldProgressTime
        )

        // 漏接判定 (改用 BAD_WINDOW 並顯示 Miss 判定文字)
        notes.filter { !it.hit && it.time < now - BAD_WINDOW }.forEach { missedNote ->
            missedNote.hit = true
            combo = 0
            showCombo = false
            showJudgmentText("Miss")
        }

        // Combo 顯示與動畫
        if (showCombo && combo > 0) {
            val centerX = width - 80f
            val centerY = 700f

            val elapsed = System.currentTimeMillis() - comboBounceTime
            comboScale = if (elapsed < 150) {
                1.2f + 0.2f * sin((elapsed / 150f) * Math.PI).toFloat()
            } else {
                1.0f
            }

            canvas.save()
            canvas.scale(comboScale, comboScale, centerX, centerY)
            canvas.drawText("$combo", centerX - 110f, centerY, comboPaint)
            canvas.drawText("COMBO", centerX, centerY + 80f, comboPaint)
            canvas.restore()
        }

        // 判定文字彈跳動畫與顯示控制
        judgmentText?.let { text ->
            val elapsed = System.currentTimeMillis() - judgmentBounceStartTime

            // 動畫時間前 150ms 放大縮小彈跳
            judgmentScale = if (elapsed < 150) {
                1.2f + 0.2f * sin((elapsed / 150f) * Math.PI).toFloat()
            } else {
                1.0f
            }

            // 動畫結束後停留 1000ms，再自動隱藏文字
            if (elapsed > 1150) {
                judgmentVisible = false
                judgmentText = null
            }

            if (judgmentVisible) {
                canvas.save()
                canvas.scale(judgmentScale, judgmentScale, width / 2f, height - 300f)
                canvas.drawText(text, width / 2f, height - 300f, judgmentPaint)
                canvas.restore()
            }
        }

        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val now = System.currentTimeMillis() - startTime

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 長按起點判定
                notes.find {
                    it.type == 2.0 && !it.hit && judge.isNoteTouched(it, x, y, now, fieldRenderer.lineStartX, fieldRenderer.lineEndX, fieldRenderer.lineEndY, height)
                }?.let { start ->
                    val end = notes.find { it.type == 2.5 && it.lane == start.lane && it.time > start.time && !it.hit }
                    if (end != null) {
                        val judgment = judgeTiming(start.time, now)
                        if (judgment != "Miss") {
                            holdingNote = start
                            holdEndNote = end
                            isHolding = true
                            currentHoldProgressTime = start.time

                            // 只有 Perfect 跟 Great 才加 combo
                            if (judgment == "Perfect" || judgment == "Great") {
                                combo++
                                showCombo = true
                                comboBounceTime = System.currentTimeMillis()
                            } else {
                                combo = 0
                                showCombo = false
                            }

                            showJudgmentText(judgment)
                        } else {
                            combo = 0
                            showCombo = false
                            showJudgmentText("Miss")
                        }
                        invalidate()
                        return true
                    }
                }

                // 單擊判定
                notes.find { it.type == 1.0 && !it.hit && judge.isNoteTouched(it, x, y, now, fieldRenderer.lineStartX, fieldRenderer.lineEndX, fieldRenderer.lineEndY, height) }?.let { note ->
                    val judgment = judgeTiming(note.time, now)
                    if (judgment != "Miss") {
                        note.hit = true
                        // 只有 Perfect 跟 Great 才加 combo
                        if (judgment == "Perfect" || judgment == "Great") {
                            combo++
                            showCombo = true
                            comboBounceTime = System.currentTimeMillis()
                        } else {
                            combo = 0
                            showCombo = false
                        }
                        showJudgmentText(judgment)
                    } else {
                        combo = 0
                        showCombo = false
                        showJudgmentText("Miss")
                    }
                    invalidate()
                    return true
                }

                // 上滑判定
                notes.find { it.type == 3.0 && !it.hit && judge.isNoteTouched(it, x, y, now, fieldRenderer.lineStartX, fieldRenderer.lineEndX, fieldRenderer.lineEndY, height) }?.let { note ->
                    val judgment = judgeTiming(note.time, now)
                    if (judgment != "Miss") {
                        note.hit = true
                        // 只有 Perfect 跟 Great 才加 combo
                        if (judgment == "Perfect" || judgment == "Great") {
                            combo++
                            if (combo > GameResult.maxCombo) {
                                GameResult.maxCombo = combo
                            }
                            showCombo = true
                            comboBounceTime = System.currentTimeMillis()
                        } else {
                            combo = 0
                            showCombo = false
                        }
                        showJudgmentText(judgment)
                    } else {
                        combo = 0
                        showCombo = false
                        showJudgmentText("Miss")
                    }
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
                        combo = 0
                        showCombo = false

                        holdingNote?.hit = true
                        holdEndNote?.hit = true

                        showJudgmentText("Miss")
                        cancelHold()
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isHolding && holdingNote != null && holdEndNote != null) {
                    if (currentHoldProgressTime < holdEndNote!!.time) {
                        combo = 0
                        showCombo = false
                        holdingNote?.hit = true
                        holdEndNote?.hit = true
                        showJudgmentText("Miss")
                    } else {
                        holdingNote?.hit = true
                        holdEndNote?.hit = true
                    }
                }
                isHolding = false
                holdingNote = null
                holdEndNote = null
                invalidate()
            }
        }

        return true
    }

    private fun judgeTiming(noteTime: Long, now: Long): String {
        val delta = abs(noteTime - now)
        return when {
            delta <= PERFECT_WINDOW -> "Perfect"
            delta <= GREAT_WINDOW -> "Great"
            delta <= GOOD_WINDOW -> "Good"
            delta <= BAD_WINDOW -> "Bad"
            else -> "Miss"
        }
    }

    private fun showJudgmentText(text: String) {
        when (text) {
            "Perfect" -> GameResult.perfectCount++
            "Great" -> GameResult.greatCount++
            "Good" -> GameResult.goodCount++
            "Bad" -> GameResult.badCount++
            "Miss" -> GameResult.missCount++
        }
        judgmentText = text
        judgmentBounceStartTime = System.currentTimeMillis()
        judgmentVisible = true
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
