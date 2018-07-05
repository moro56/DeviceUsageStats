package it.emperor.deviceusagestats.ui.network.model

import android.app.usage.NetworkStats
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class NetworkStatsMap(val packageManager: PackageManager) {

    internal var rxByTime: MutableMap<Long, Double> = mutableMapOf()
    internal var txByTime: MutableMap<Long, Double> = mutableMapOf()
    internal var rxByApp: MutableMap<Int, NetworkStatsInternal> = mutableMapOf()
    internal var txByApp: MutableMap<Int, NetworkStatsInternal> = mutableMapOf()
    internal var rxByAppTotal: Long = 0
    internal var txByAppTotal: Long = 0

    fun updateRxByTime(index: Long, value: Double?, maxValue: Long): Long {
        value?.let {
            rxByTime[index]?.let {
                rxByTime[index] = rxByTime[index]!! + value
            } ?: rxByTime.put(index, value)

            if (rxByTime[index]!! > maxValue) {
                return rxByTime[index]!!.toLong()
            }
            return maxValue
        } ?: kotlin.run {
            rxByTime[index] ?: rxByTime.put(index, 0.0)
            return maxValue
        }
    }

    fun updateTxByTime(index: Long, value: Double?, maxValue: Long): Long {
        value?.let {
            txByTime[index]?.let {
                txByTime[index] = txByTime[index]!! + value
            } ?: txByTime.put(index, value)

            if (txByTime[index]!! > maxValue) {
                return txByTime[index]!!.toLong()
            }
            return maxValue
        } ?: kotlin.run {
            txByTime[index] ?: txByTime.put(index, 0.0)
            return maxValue
        }
    }

    fun updateByApp(bucket: NetworkStats.Bucket) {
        if (rxByApp.containsKey(bucket.uid)) {
            rxByApp[bucket.uid]!!.valueDownload = rxByApp[bucket.uid]!!.valueDownload + bucket.rxBytes
            txByApp[bucket.uid]!!.valueUpload = txByApp[bucket.uid]!!.valueUpload + bucket.txBytes
        } else {
            var icon: Drawable? = null
            var name = "Unknown"
            var packageName = name

            val wifiNetworkUsage = NetworkStatsInternal()
            wifiNetworkUsage.uid = bucket.uid
            wifiNetworkUsage.valueDownload = bucket.rxBytes
            wifiNetworkUsage.valueUpload = bucket.txBytes
            try {
                val applicationInfo = packageManager.getApplicationInfo(packageManager.getNameForUid(bucket.uid), PackageManager.GET_META_DATA)
                try {
                    icon = packageManager.getApplicationIcon(applicationInfo)
                } catch (ex2: PackageManager.NameNotFoundException) {
                }
                name = packageManager.getApplicationLabel(applicationInfo).toString()
                packageName = applicationInfo.packageName
            } catch (ex: PackageManager.NameNotFoundException) {
                packageManager.getNameForUid(bucket.uid)?.let { packageName = it }
            }
            wifiNetworkUsage.name = name
            wifiNetworkUsage.packageName = packageName
            wifiNetworkUsage.icon = icon

            rxByApp[bucket.uid] = wifiNetworkUsage
            txByApp[bucket.uid] = wifiNetworkUsage
        }

        rxByAppTotal += bucket.rxBytes
        txByAppTotal += bucket.txBytes
    }

    fun clear() {
        rxByTime.clear()
        txByTime.clear()
        rxByApp.clear()
        txByApp.clear()
        rxByAppTotal = 0
        txByAppTotal = 0
    }
}