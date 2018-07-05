package it.emperor.deviceusagestats.ui.network.model

import android.app.usage.NetworkStats
import android.content.pm.PackageManager

enum class NetworkStatsMapsType {
    WIFI_TIME_RX, WIFI_TIME_TX, WIFI_APP, MOBILE_TIME_RX, MOBILE_TIME_TX, MOBILE_APP
}

enum class NetworkStatsMapsTimeType {
    HOUR, DAY, MONTH
}

data class NetworkStatsMaps(val packageManager: PackageManager) {

    internal val all: NetworkStatsMap = NetworkStatsMap(packageManager)
    internal val wifi: NetworkStatsMap = NetworkStatsMap(packageManager)
    internal val mobile: NetworkStatsMap = NetworkStatsMap(packageManager)
    internal val background: NetworkStatsMap = NetworkStatsMap(packageManager)

    internal var rxByTime: MutableList<Long> = mutableListOf()

    internal var rxWifiTotal: Long = 0
    internal var txWifiTotal: Long = 0
    internal var rxMobileTotal: Long = 0
    internal var txMobileTotal: Long = 0
    internal var rxBackgroundTotal: Long = 0
    internal var txBackgroundTotal: Long = 0
    internal var rxForegroundTotal: Long = 0
    internal var txForegroundTotal: Long = 0
    internal var maxValue: Long = 0

    fun update(wifiTotal: NetworkStats.Bucket?, mobileTotal: NetworkStats.Bucket?, timeType: NetworkStatsMapsTimeType, wifiDetails: NetworkStats?, mobileDetails: NetworkStats?, wifiSummary: NetworkStats?, mobileSummary: NetworkStats?) {
        all.clear()
        wifi.clear()
        mobile.clear()
        background.clear()

        rxByTime.clear()

        rxWifiTotal = wifiTotal?.rxBytes ?: 0
        txWifiTotal = wifiTotal?.txBytes ?: 0
        rxMobileTotal = mobileTotal?.rxBytes ?: 0
        txMobileTotal = mobileTotal?.txBytes ?: 0
        rxBackgroundTotal = 0
        txBackgroundTotal = 0
        rxForegroundTotal = 0
        txForegroundTotal = 0
        maxValue = 0

        val timeDivider = getTimeDividet(timeType)

        if (wifiDetails != null) {
            while (wifiDetails.hasNextBucket()) {
                val bucket: NetworkStats.Bucket = NetworkStats.Bucket()
                wifiDetails.getNextBucket(bucket)
                addValue(NetworkStatsMapsType.WIFI_TIME_RX, timeDivider, bucket.endTimeStamp, bucket.rxBytes.toDouble())
                addValue(NetworkStatsMapsType.WIFI_TIME_TX, timeDivider, bucket.endTimeStamp, bucket.txBytes.toDouble())
                addValue(NetworkStatsMapsType.WIFI_APP, bucket)
            }
            wifiDetails.close()
        }

        if (wifiSummary != null) {
            while (wifiSummary.hasNextBucket()) {
                val bucket: NetworkStats.Bucket = NetworkStats.Bucket()
                wifiSummary.getNextBucket(bucket)
                addValueForBackground(NetworkStatsMapsType.WIFI_TIME_RX, bucket.rxBytes.toDouble(), bucket.state)
                addValueForBackground(NetworkStatsMapsType.WIFI_TIME_TX, bucket.txBytes.toDouble(), bucket.state)
            }
            wifiSummary.close()
        }

        if (mobileDetails != null) {
            while (mobileDetails.hasNextBucket()) {
                val bucket: NetworkStats.Bucket = NetworkStats.Bucket()
                mobileDetails.getNextBucket(bucket)
                addValue(NetworkStatsMapsType.MOBILE_TIME_RX, timeDivider, bucket.endTimeStamp, bucket.rxBytes.toDouble())
                addValue(NetworkStatsMapsType.MOBILE_TIME_TX, timeDivider, bucket.endTimeStamp, bucket.txBytes.toDouble())
                addValue(NetworkStatsMapsType.MOBILE_APP, bucket)
            }
            mobileDetails.close()
        }

        if (mobileSummary != null) {
            while (mobileSummary.hasNextBucket()) {
                val bucket: NetworkStats.Bucket = NetworkStats.Bucket()
                mobileSummary.getNextBucket(bucket)
                addValueForBackground(NetworkStatsMapsType.MOBILE_TIME_RX, bucket.rxBytes.toDouble(), bucket.state)
                addValueForBackground(NetworkStatsMapsType.MOBILE_TIME_TX, bucket.txBytes.toDouble(), bucket.state)
            }
            mobileSummary.close()
        }

        maxValue += (maxValue * 0.2f).toLong()

        for ((key, _) in wifi.rxByTime) {
            rxByTime.add(key)
        }
        rxByTime.sort()
    }

    fun update(wifiDetails: NetworkStats?, mobileDetails: NetworkStats?) {
        all.clear()
        wifi.clear()
        mobile.clear()

        if (wifiDetails != null) {
            while (wifiDetails.hasNextBucket()) {
                val bucket: NetworkStats.Bucket = NetworkStats.Bucket()
                wifiDetails.getNextBucket(bucket)
                addValue(NetworkStatsMapsType.WIFI_APP, bucket)
            }
            wifiDetails.close()
        }

        if (mobileDetails != null) {
            while (mobileDetails.hasNextBucket()) {
                val bucket: NetworkStats.Bucket = NetworkStats.Bucket()
                mobileDetails.getNextBucket(bucket)
                addValue(NetworkStatsMapsType.MOBILE_APP, bucket)
            }
            mobileDetails.close()
        }
    }

    private fun getTimeDividet(timeType: NetworkStatsMapsTimeType): Long {
        return when (timeType) {
            NetworkStatsMapsTimeType.HOUR -> 1000
            NetworkStatsMapsTimeType.DAY -> 86400000
            NetworkStatsMapsTimeType.MONTH -> 2592000000
        }
    }

    private fun addValue(type: NetworkStatsMapsType, timeDivider: Long, key: Long, value: Double) {
        val index = key / timeDivider
        when (type) {
            NetworkStatsMapsType.WIFI_TIME_RX -> {
                maxValue = wifi.updateRxByTime(index, value, maxValue)
                mobile.updateRxByTime(index, null, maxValue)
            }
            NetworkStatsMapsType.WIFI_TIME_TX -> {
                maxValue = wifi.updateTxByTime(index, value, maxValue)
                mobile.updateTxByTime(index, null, maxValue)
            }
            NetworkStatsMapsType.MOBILE_TIME_RX -> {
                maxValue = mobile.updateRxByTime(index, value, maxValue)
                wifi.updateRxByTime(index, null, maxValue)
            }
            NetworkStatsMapsType.MOBILE_TIME_TX -> {
                maxValue = mobile.updateTxByTime(index, value, maxValue)
                wifi.updateTxByTime(index, null, maxValue)
            }
            else -> {
            }
        }
    }

    private fun addValueForBackground(type: NetworkStatsMapsType, value: Double, state: Int) {
        when (type) {
            NetworkStatsMapsType.WIFI_TIME_RX -> {
                if (state == NetworkStats.Bucket.STATE_DEFAULT) {
                    rxBackgroundTotal += value.toLong()
                } else {
                    rxForegroundTotal += value.toLong()
                }
            }
            NetworkStatsMapsType.WIFI_TIME_TX -> {
                if (state == NetworkStats.Bucket.STATE_DEFAULT) {
                    txBackgroundTotal += value.toLong()
                } else {
                    txForegroundTotal += value.toLong()
                }
            }
            NetworkStatsMapsType.MOBILE_TIME_RX -> {
                if (state == NetworkStats.Bucket.STATE_DEFAULT) {
                    rxBackgroundTotal += value.toLong()
                } else {
                    rxForegroundTotal += value.toLong()
                }
            }
            NetworkStatsMapsType.MOBILE_TIME_TX -> {
                if (state == NetworkStats.Bucket.STATE_DEFAULT) {
                    txBackgroundTotal += value.toLong()
                } else {
                    txForegroundTotal += value.toLong()
                }
            }
            else -> {
            }
        }
    }

    private fun addValue(type: NetworkStatsMapsType, bucket: NetworkStats.Bucket) {
        when (type) {
            NetworkStatsMapsType.WIFI_APP -> {
                all.updateByApp(bucket)
                wifi.updateByApp(bucket)
            }
            NetworkStatsMapsType.MOBILE_APP -> {
                all.updateByApp(bucket)
                mobile.updateByApp(bucket)
            }
            else -> {
            }
        }
    }
}