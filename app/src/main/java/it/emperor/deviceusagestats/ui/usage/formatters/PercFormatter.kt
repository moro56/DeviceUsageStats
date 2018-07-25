package it.emperor.deviceusagestats.ui.usage.formatters

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler

class PercFormatter() : IValueFormatter {
    override fun getFormattedValue(value: Float, entry: Entry?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?): String {
        return "%.0f%%".format(value)
    }
}