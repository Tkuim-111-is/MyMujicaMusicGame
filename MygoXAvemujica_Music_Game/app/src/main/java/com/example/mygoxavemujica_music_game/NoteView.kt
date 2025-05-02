package com.example.mygoxavemujica_music_game

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View

class NoteView(context: Context, private val notes: List<Note>) : View(context) {
    private val paint = Paint()
    private val startTime = System.currentTimeMillis()
    private val speed = 1.0f
    private val laneLineCount = 8
    private val zMax = 1000f

    private val lineStartX = FloatArray(laneLineCount)
    private val lineStartY = FloatArray(laneLineCount)
    private val lineEndX = FloatArray(laneLineCount)
    private val lineEndY = FloatArray(laneLineCount)

    private val noteImage1 = BitmapFactory.decodeResource(resources, R.raw.circle)
    private val noteImage2 = BitmapFactory.decodeResource(resources, R.raw.longtopras)
    private val noteImage3 = BitmapFactory.decodeResource(resources, R.raw.jump)

    private var holdingNote: Note? = null
    private var holdEndNote: Note? = null
    private var isHolding = false
    private var currentHoldProgressTime: Long = 0L

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val judgeY = height - 200f
        val now = System.currentTimeMillis() - startTime
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

        paint.color = Color.RED
        paint.strokeWidth = 20f
        canvas.drawLine(0f, judgeY, width.toFloat(), judgeY, paint)

        for (note in notes) {
            if (!note.hit) {
                when (note.type) {
                    1.0 -> drawTapNote(canvas, note, now, judgeY)
                    2.0 -> {
                        val endNote = notes.find { it.type == 2.5 && it.lane == note.lane && it.time > note.time && !it.hit }
                        if (endNote != null) {
                            drawHoldBar(canvas, note, endNote, now, lineStartX, lineEndX, judgeY, lineEndY, zMax)
                        }
                    }
                    3.0 -> drawFlickNote(canvas, note, now, judgeY)
                }
            }
        }

        invalidate()
    }

    //單擊按鈕
    private fun drawTapNote(canvas: Canvas, note: Note, now: Long, judgeY: Float) {
        val i = note.lane
        if (i < 0 || i >= laneLineCount - 1) return

        val z = (note.time - now) * speed
        if (z > zMax) return
        val t = z / zMax

        val leftX = lerp(lineStartX[i], lineEndX[i], t)
        val rightX = lerp(lineStartX[i + 1], lineEndX[i + 1], t)
        val x = (leftX + rightX) / 2f
        val y = lerp(judgeY, lineEndY[i], t)

        val rawScale = 1f / (1f + z * 0.003f)
        val scale = rawScale.coerceAtMost(0.75f)
        val newWidth = noteImage1.width * scale
        val newHeight = noteImage1.height * scale

        val left = x - newWidth / 2f
        val top = y - newHeight / 2f
        val right = x + newWidth / 2f
        val bottom = y + newHeight / 2f

        canvas.drawBitmap(noteImage1, null, RectF(left, top, right, bottom), paint)
    }

    //上滑按鈕
    private fun drawFlickNote(canvas: Canvas, note: Note, now: Long, judgeY: Float) {
        val i = note.lane
        if (i < 0 || i >= laneLineCount - 1) return

        val z = (note.time - now) * speed
        if (z > zMax) return
        val t = z / zMax

        val leftX = lerp(lineStartX[i], lineEndX[i], t)
        val rightX = lerp(lineStartX[i + 1], lineEndX[i + 1], t)
        val x = (leftX + rightX) / 2f
        val y = lerp(judgeY, lineEndY[i], t)

        val rawScale = 1f / (1f + z * 0.003f)
        val scale = rawScale.coerceAtMost(0.75f)
        val newWidth = noteImage3.width * scale
        val newHeight = noteImage3.height * scale

        val left = x - newWidth / 2f
        val top = y - newHeight / 2f
        val right = x + newWidth / 2f
        val bottom = y + newHeight / 2f

        canvas.drawBitmap(noteImage3, null, RectF(left, top, right, bottom), paint)
    }

    //長按按鈕
    private fun drawHoldBar(
        canvas: Canvas,
        startNote: Note,
        endNote: Note,
        now: Long,
        lineStartX: FloatArray,
        lineEndX: FloatArray,
        judgeY: Float,
        lineEndY: FloatArray,
        zMax: Float
    ) {
        val i = startNote.lane
        if (i < 0 || i >= lineStartX.size - 1) return

        // 如果正在長按，起點位置固定在判定線（judgeY）
        val zStart = if (isHolding && holdingNote == startNote && holdEndNote == endNote) 0f else (startNote.time - now) * speed
        val tStart = zStart / zMax

        // 尾端位置根據當前進度時間或尾端時間計算
        val endTimeDynamic = if (isHolding && holdingNote == startNote && holdEndNote == endNote) currentHoldProgressTime else endNote.time
        val zEnd = (endTimeDynamic - now) * speed
        val tEnd = zEnd / zMax

        val shrink = 20f
        val xLeftStart = lerp(lineStartX[i], lineEndX[i], tStart) + shrink
        val xRightStart = lerp(lineStartX[i + 1], lineEndX[i + 1], tStart) - shrink
        val xLeftEnd = lerp(lineStartX[i], lineEndX[i], tEnd) + shrink
        val xRightEnd = lerp(lineStartX[i + 1], lineEndX[i + 1], tEnd) - shrink

        val yStart = lerp(judgeY, lineEndY[i], tStart)
        val yEnd = lerp(judgeY, lineEndY[i], tEnd)

        // 繪製長按條矩形
        val path = Path().apply {
            moveTo(xLeftStart, yStart)
            lineTo(xRightStart, yStart)
            lineTo(xRightEnd, yEnd)
            lineTo(xLeftEnd, yEnd)
            close()
        }

        paint.color = Color.argb(150, 0, 255, 0)
        canvas.drawPath(path, paint)

        // 繪製起點圖標（僅在未長按且未擊中時顯示）
        if (!startNote.hit && !(isHolding && holdingNote == startNote && holdEndNote == endNote)) {
            val rawScaleStart = 1f / (1f + zStart * 0.003f)
            val scaleStart = rawScaleStart.coerceAtMost(0.75f)
            val wStart = noteImage2.width * scaleStart
            val hStart = noteImage2.height * scaleStart

            val xStart = (xLeftStart + xRightStart) / 2f
            val yStartFinal = yStart

            val leftStart = xStart - wStart / 2f
            val topStart = yStartFinal - hStart / 2f
            val rightStart = xStart + wStart / 2f
            val bottomStart = yStartFinal + hStart / 2f

            canvas.drawBitmap(noteImage2, null, RectF(leftStart, topStart, rightStart, bottomStart), paint)
        }

        // 繪製尾端圖標
        if (!endNote.hit) {
            val rawScaleEnd = 1f / (1f + zEnd * 0.003f)
            val scaleEnd = rawScaleEnd.coerceAtMost(0.75f)
            val wEnd = noteImage2.width * scaleEnd
            val hEnd = noteImage2.height * scaleEnd

            val xEnd = (xLeftEnd + xRightEnd) / 2f
            val yEndFinal = yEnd

            val leftEnd = xEnd - wEnd / 2f
            val topEnd = yEndFinal - hEnd / 2f
            val rightEnd = xEnd + wEnd / 2f
            val bottomEnd = yEndFinal + hEnd / 2f

            canvas.drawBitmap(noteImage2, null, RectF(leftEnd, topEnd, rightEnd, bottomEnd), paint)
        }
    }

    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start * (1 - t) + end * t
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val now = System.currentTimeMillis() - startTime

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 長按起點
                notes.find { it.type == 2.0 && !it.hit && isNoteTouched(it, x, y, now) }?.let { start ->
                    holdingNote = start
                    start.hit = true // 起點立即消失
                    isHolding = true
                    currentHoldProgressTime = start.time // 初始化進度時間
                    invalidate()
                }

                // 普通音符
                notes.find { it.type == 1.0 && !it.hit && isNoteTouched(it, x, y, now) }?.let {
                    it.hit = true
                    invalidate()
                }

                // 上滑音符
                notes.find { it.type == 3.0 && !it.hit && isNoteTouched(it, x, y, now) }?.let {
                    it.hit = true
                    invalidate()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isHolding && holdingNote != null && holdEndNote != null) {
                    if (isTouchInHoldRect(x, y, holdingNote!!, holdEndNote!!, now)) {
                        currentHoldProgressTime = now.coerceAtMost(holdEndNote!!.time)
                        invalidate()
                    } else {
                        cancelHold() // 滑出範圍，失敗
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isHolding && holdingNote != null && holdEndNote != null) {
                    if (currentHoldProgressTime >= holdEndNote!!.time - 100) { // 成功條件：進度時間接近尾端時間
                        holdEndNote?.hit = true
                    } else {
                        cancelHold() // 中途放開，失敗
                    }
                    isHolding = false
                    holdingNote = null
                    holdEndNote = null
                    invalidate()
                }
            }
        }
        return true
    }

    private fun isNoteTouched(note: Note, x: Float, y: Float, now: Long): Boolean {
        val i = note.lane
        if (i < 0 || i >= laneLineCount - 1) return false
        val z = (note.time - now) * speed
        if (z > zMax || z < -300) return false
        val t = z / zMax
        val cx = (lerp(lineStartX[i], lineEndX[i], t) + lerp(lineStartX[i + 1], lineEndX[i + 1], t)) / 2
        val cy = lerp(height - 200f, lineEndY[i], t)
        val scale = (1f / (1f + z * 0.003f)).coerceAtMost(0.75f)
        val width = noteImage1.width * scale
        val height = noteImage1.height * scale
        return RectF(cx - width / 2, cy - height / 2, cx + width / 2, cy + height / 2).contains(x, y)
    }

    private fun isTouchInHoldRect(x: Float, y: Float, startNote: Note, endNote: Note, now: Long): Boolean {
        val i = startNote.lane
        if (i < 0 || i >= laneLineCount - 1) return false

        val zStart = if (isHolding && holdingNote == startNote) 0f else (startNote.time - now) * speed
        val tStart = zStart / zMax
        val zEnd = (currentHoldProgressTime - now) * speed
        val tEnd = zEnd / zMax

        val shrink = 20f
        val xLeftStart = lerp(lineStartX[i], lineEndX[i], tStart) + shrink
        val xRightStart = lerp(lineStartX[i + 1], lineEndX[i + 1], tStart) - shrink
        val xLeftEnd = lerp(lineStartX[i], lineEndX[i], tEnd) + shrink
        val xRightEnd = lerp(lineStartX[i + 1], lineEndX[i + 1], tEnd) - shrink

        val yStart = lerp(height - 200f, lineEndY[i], tStart)
        val yEnd = lerp(height - 200f, lineEndY[i], tEnd)

        val minX = minOf(xLeftStart, xRightStart, xLeftEnd, xRightEnd)
        val maxX = maxOf(xLeftStart, xRightStart, xLeftEnd, xRightEnd)
        val minY = minOf(yStart, yEnd)
        val maxY = maxOf(yStart, yEnd)

        return x in minX..maxX && y in minY..maxY
    }

    private fun cancelHold() {
        holdingNote?.hit = true
        holdEndNote?.hit = true
        isHolding = false
        holdingNote = null
        holdEndNote = null
        invalidate()
    }
}