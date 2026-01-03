package com.zhengdesheng.z202304100318.ademo.ui.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.cos
import kotlin.math.sin

class WheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val items = mutableListOf<String>()
    private val colors = mutableListOf<Int>()

    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var currentAngle = 0f
    private var targetAngle = 0f

    private var animator: ValueAnimator? = null
    private var onWheelResultListener: ((String) -> Unit)? = null

    init {
        setupPaints()
        setupDefaultColors()
    }

    private fun setupPaints() {
        paint.style = Paint.Style.FILL
        textPaint.color = Color.WHITE
        textPaint.textSize = 48f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD

        pointerPaint.color = Color.RED
        pointerPaint.style = Paint.Style.FILL
    }

    private fun setupDefaultColors() {
        colors.apply {
            add(Color.parseColor("#FF6B6B"))
            add(Color.parseColor("#4ECDC4"))
            add(Color.parseColor("#45B7D1"))
            add(Color.parseColor("#96CEB4"))
            add(Color.parseColor("#FFEAA7"))
            add(Color.parseColor("#DDA0DD"))
            add(Color.parseColor("#98D8C8"))
            add(Color.parseColor("#F7DC6F"))
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = (minOf(w, h) / 2f) * 0.9f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (items.isEmpty()) {
            drawEmptyWheel(canvas)
            return
        }

        drawWheel(canvas)
        drawPointer(canvas)
    }

    private fun drawEmptyWheel(canvas: Canvas) {
        paint.color = Color.LTGRAY
        canvas.drawCircle(centerX, centerY, radius, paint)

        textPaint.textSize = 40f
        textPaint.color = Color.DKGRAY
        canvas.drawText("添加选项", centerX, centerY, textPaint)
    }

    private fun drawWheel(canvas: Canvas) {
        val sliceAngle = 360f / items.size

        for (i in items.indices) {
            val startAngle = currentAngle + i * sliceAngle
            val endAngle = startAngle + sliceAngle

            paint.color = colors[i % colors.size]
            canvas.drawArc(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius,
                startAngle,
                sliceAngle,
                true,
                paint
            )

            drawTextInSlice(canvas, items[i], startAngle, sliceAngle)
        }
    }

    private fun drawTextInSlice(canvas: Canvas, text: String, startAngle: Float, sliceAngle: Float) {
        val midAngle = Math.toRadians((startAngle + sliceAngle / 2).toDouble())
        val textRadius = radius * 0.65f

        val x = centerX + textRadius * cos(midAngle).toFloat()
        val y = centerY + textRadius * sin(midAngle).toFloat()

        canvas.save()
        canvas.rotate((startAngle + sliceAngle / 2 + 90).toFloat(), x, y)
        textPaint.textSize = 36f
        canvas.drawText(text, x, y, textPaint)
        canvas.restore()
    }

    private fun drawPointer(canvas: Canvas) {
        val pointerSize = radius * 0.15f
        val pointerPath = Path()

        pointerPath.moveTo(centerX + radius + 20f, centerY)
        pointerPath.lineTo(centerX + radius + 20f - pointerSize, centerY - pointerSize / 2)
        pointerPath.lineTo(centerX + radius + 20f - pointerSize, centerY + pointerSize / 2)
        pointerPath.close()

        canvas.drawPath(pointerPath, pointerPaint)
    }

    fun setItems(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        invalidate()
    }

    fun addItem(item: String) {
        items.add(item)
        invalidate()
    }

    fun removeItem(item: String) {
        items.remove(item)
        invalidate()
    }

    fun spin() {
        if (items.isEmpty() || animator?.isRunning == true) return

        val randomSpins = (5..10).random()
        val randomAngle = (0..360).random().toFloat()
        targetAngle = currentAngle + randomSpins * 360f + randomAngle

        animator = ValueAnimator.ofFloat(currentAngle, targetAngle).apply {
            duration = 4000L
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                currentAngle = animation.animatedValue as Float
                invalidate()
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    val result = calculateResult()
                    onWheelResultListener?.invoke(result)
                }
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            start()
        }
    }

    private fun calculateResult(): String {
        if (items.isEmpty()) return ""
        
        // 计算每个切片的角度
        val sliceAngle = 360f / items.size
        
        // 指针在右侧，固定在0度位置（Android Canvas中0度在右侧）
        val pointerAngle = 0f
        
        android.util.Log.d("WheelView", "Current angle: $currentAngle, Pointer angle: $pointerAngle")
        
        // 遍历所有切片，找出包含指针的那个
        for (i in items.indices) {
            // 计算切片的起始和结束角度（考虑转盘当前旋转的角度）
            // 当转盘旋转currentAngle度后，每个切片也跟着旋转了currentAngle度
            val sliceStart = (i * sliceAngle + currentAngle) % 360
            val sliceEnd = ((i + 1) * sliceAngle + currentAngle) % 360
            
            // 添加调试日志
            android.util.Log.d("WheelView", "Slice $i: $sliceStart to $sliceEnd, Item: ${items[i]}")
            
            // 检查指针是否在这个切片范围内
            if (sliceStart < sliceEnd) {
                // 正常情况：切片在0-360度范围内
                if (pointerAngle >= sliceStart && pointerAngle < sliceEnd) {
                    android.util.Log.d("WheelView", "Selected slice: $i, Item: ${items[i]}")
                    return items[i]
                }
            } else {
                // 边界情况：切片跨越了360度边界
                if (pointerAngle >= sliceStart || pointerAngle < sliceEnd) {
                    android.util.Log.d("WheelView", "Selected slice (cross boundary): $i, Item: ${items[i]}")
                    return items[i]
                }
            }
        }
        
        // 如果没有找到匹配的切片，返回第一个选项
        android.util.Log.d("WheelView", "No slice found, returning first item: ${items[0]}")
        return items[0]
    }

    fun setOnWheelResultListener(listener: (String) -> Unit) {
        onWheelResultListener = listener
    }

    fun reset() {
        animator?.cancel()
        currentAngle = 0f
        targetAngle = 0f
        invalidate()
    }
}
