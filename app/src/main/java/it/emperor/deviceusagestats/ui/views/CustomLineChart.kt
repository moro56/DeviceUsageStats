package it.emperor.deviceusagestats.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import com.github.mikephil.charting.charts.LineChart
import it.emperor.deviceusagestats.R

class CustomLineChart : LineChart {

    private val paint: Paint = Paint()

    private var highlight: Boolean = false
    private var position: Int = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    init {
        paint.isAntiAlias = true
        paint.color = context.getColor(R.color.system_background_dark)
        paint.style = Paint.Style.FILL
        paint.alpha = 175
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (highlight && highlighted != null && highlighted.isNotEmpty()) {
            val firstItem = highlighted[0]
            val xRendererBuffer: MutableList<Float> = mutableListOf()
            for (i in 0 until mXAxisRenderer.mRenderGridLinesBuffer.size) {
                if (i % 2 == 0) {
                    xRendererBuffer.add(mXAxisRenderer.mRenderGridLinesBuffer[i])
                }
            }
            val posX = firstItem.drawX
            val firstVisibleItem = if (lowestVisibleX > lowestVisibleX.toInt().toFloat()) lowestVisibleX.toInt() + 1 else lowestVisibleX.toInt()
            val prevPosition = position - 1 - firstVisibleItem
            var prevPosX = 0f
            if (prevPosition >= 0) {
                prevPosX = try {
                    xRendererBuffer[(position - 1 - firstVisibleItem)]
                } catch (ex: Exception) {
                    (width * 2).toFloat()
                }
            }

            canvas?.drawRect(prevPosX, 0f, posX, firstItem.drawY, paint)
        }
    }

    fun highightSpace(position: Int) {
        this.position = position
        this.highlight = true
        invalidate()
    }

    fun hideHighlight() {
        this.highlight = false
        highlightValues(null)
    }
}
