package it.emperor.deviceusagestats.ui.network.formatters

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import it.emperor.deviceusagestats.ui.network.model.NetworkStatsMapsTimeType
import org.joda.time.DateTime

class TimeAxisFormatter(private val timeType: NetworkStatsMapsTimeType, private val times: List<Long>) : IAxisValueFormatter {

    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        try {
            val realValue = times[value.toInt()] * getTimeDividet(timeType)
            when (timeType) {
                NetworkStatsMapsTimeType.HOUR -> return DateTime(realValue).toString("HH:mm")
                NetworkStatsMapsTimeType.DAY -> return DateTime(realValue).toString("dd MMM")
                NetworkStatsMapsTimeType.MONTH -> return DateTime(realValue).toString("MMM")
            }
        } catch (ex: Exception) {
            return ""
        }
    }

    private fun getTimeDividet(timeType: NetworkStatsMapsTimeType): Long {
        when (timeType) {
            NetworkStatsMapsTimeType.HOUR -> return 1000
            NetworkStatsMapsTimeType.DAY -> return 86400000
            NetworkStatsMapsTimeType.MONTH -> return 2592000000
        }
    }
}