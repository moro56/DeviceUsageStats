package it.emperor.deviceusagestats.ui.network.formatters

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import it.emperor.deviceusagestats.models.TimeType
import org.joda.time.DateTime

class TimeAxisFormatter(private val timeType: TimeType, private val times: List<Long>) : IAxisValueFormatter {

    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        try {
            val realValue = times[value.toInt()] * getTimeDividet(timeType)
            when (timeType) {
                TimeType.TODAY -> return DateTime(realValue).toString("HH:mm")
                TimeType.WEEK -> return DateTime(realValue).toString("dd MMM")
                TimeType.MONTH -> return DateTime(realValue).toString("dd MMM")
                TimeType.LAST_MONTH -> return DateTime(realValue).toString("dd MMM")
                TimeType.YEAR -> return DateTime(realValue).toString("MMM")
                TimeType.CUSTOM -> return DateTime(realValue).toString("dd MMM")
            }
        } catch (ex: Exception) {
            return ""
        }
    }

    private fun getTimeDividet(timeType: TimeType): Long {
        when (timeType) {
            TimeType.TODAY -> return 1000
            TimeType.WEEK -> return 86400000
            TimeType.MONTH -> return 86400000
            TimeType.LAST_MONTH -> return 86400000
            TimeType.YEAR -> return 2592000000
            TimeType.CUSTOM -> return 86400000
        }
    }
}