package it.emperor.deviceusagestats.ui.network.formatters

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import it.emperor.deviceusagestats.App
import it.emperor.deviceusagestats.R
import it.emperor.deviceusagestats.extensions.formatBytes

class NetworkAxisFormatter() : IAxisValueFormatter {

    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        return value.formatBytes()
    }
}