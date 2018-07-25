package it.emperor.deviceusagestats.ui.network.formatters

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import it.emperor.deviceusagestats.models.NetworkTimeType
import org.joda.time.DateTime

class TimeAxisFormatter(private val timeType: NetworkTimeType, private val times: List<Long>) : IAxisValueFormatter {

    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        try {
            val realValue = times[value.toInt()] * getTimeDividet(timeType)
            when (timeType) {
                NetworkTimeType.TODAY -> return DateTime(realValue).toString("HH:mm")
                NetworkTimeType.WEEK -> return DateTime(realValue).toString("dd MMM")
                NetworkTimeType.MONTH -> return DateTime(realValue).toString("dd MMM")
                NetworkTimeType.LAST_MONTH -> return DateTime(realValue).toString("dd MMM")
                NetworkTimeType.YEAR -> return DateTime(realValue).toString("MMM")
                NetworkTimeType.CUSTOM -> return DateTime(realValue).toString("dd MMM")
            }
        } catch (ex: Exception) {
            return ""
        }
    }

    private fun getTimeDividet(timeType: NetworkTimeType): Long {
        when (timeType) {
            NetworkTimeType.TODAY -> return 1000
            NetworkTimeType.WEEK -> return 86400000
            NetworkTimeType.MONTH -> return 86400000
            NetworkTimeType.LAST_MONTH -> return 86400000
            NetworkTimeType.YEAR -> return 2592000000
            NetworkTimeType.CUSTOM -> return 86400000
        }
    }
}