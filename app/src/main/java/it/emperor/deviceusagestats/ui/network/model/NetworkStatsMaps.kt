package it.emperor.deviceusagestats.ui.network.model

import android.app.usage.NetworkStats
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

enum class NetworkStatsMapsType {
    WIFI_TIME_RX, WIFI_TIME_TX, WIFI_APP, MOBILE_TIME_RX, MOBILE_TIME_TX, MOBILE_APP
}

enum class NetworkStatsMapsTimeType {
    HOUR, DAY, MONTH
}

data class NetworkStatsMaps(var rxByTime: MutableList<Long>, var rxWifiByTime: MutableMap<Long, Double>, var txWifiByTime: MutableMap<Long, Double>, var rxWifiByApp: MutableMap<Int, it.emperor.deviceusagestats.ui.network.model.NetworkStats>, var rxMobileByTime: MutableMap<Long, Double>, var txMobileByTime: MutableMap<Long, Double>, var rxMobileByApp: MutableMap<Int, it.emperor.deviceusagestats.ui.network.model.NetworkStats>) {
    constructor() : this(mutableListOf(), mutableMapOf(), mutableMapOf(), mutableMapOf(), mutableMapOf(), mutableMapOf(), mutableMapOf())

    internal var maxValue: Long = 0

    fun init(packageManager: PackageManager, timeType: NetworkStatsMapsTimeType, wifiDetails: NetworkStats?, mobileDetails: NetworkStats?) {
        rxByTime.clear()
        rxMobileByApp.clear()
        rxMobileByTime.clear()
        txMobileByTime.clear()
        rxWifiByApp.clear()
        rxWifiByTime.clear()
        txWifiByTime.clear()
        maxValue = 0

        val timeDivider = getTimeDividet(timeType)

        if (wifiDetails != null) {
            while (wifiDetails.hasNextBucket()) {
                val bucket: NetworkStats.Bucket = NetworkStats.Bucket()
                wifiDetails.getNextBucket(bucket)
                put(NetworkStatsMapsType.WIFI_TIME_RX, timeDivider, bucket.endTimeStamp, bucket.rxBytes.toDouble())
                put(NetworkStatsMapsType.WIFI_TIME_TX, timeDivider, bucket.endTimeStamp, bucket.txBytes.toDouble())
                put(NetworkStatsMapsType.WIFI_APP, packageManager, bucket)
            }
            wifiDetails.close()
        }

        if (mobileDetails != null) {
            while (mobileDetails.hasNextBucket()) {
                val bucket: NetworkStats.Bucket = NetworkStats.Bucket()
                mobileDetails.getNextBucket(bucket)
                put(NetworkStatsMapsType.MOBILE_TIME_RX, timeDivider, bucket.endTimeStamp, bucket.rxBytes.toDouble())
                put(NetworkStatsMapsType.MOBILE_TIME_TX, timeDivider, bucket.endTimeStamp, bucket.txBytes.toDouble())
                put(NetworkStatsMapsType.MOBILE_APP, packageManager, bucket)
            }
            mobileDetails.close()
        }

        maxValue += (maxValue * 0.05f).toLong()

        for ((key, value) in rxWifiByTime) {
            rxByTime.add(key)
        }
        rxByTime.sort()
    }

    private fun getTimeDividet(timeType: NetworkStatsMapsTimeType): Long {
        when (timeType) {
            NetworkStatsMapsTimeType.HOUR -> return 1000
            NetworkStatsMapsTimeType.DAY -> return 86400000
            NetworkStatsMapsTimeType.MONTH -> return 2592000000
        }
    }

    private fun put(type: NetworkStatsMapsType, timeDivider: Long, key: Long, value: Double) {
        val index = key / timeDivider
        when (type) {
            NetworkStatsMapsType.WIFI_TIME_RX -> {
                rxWifiByTime[index]?.let {
                    rxWifiByTime[index] = rxWifiByTime[index]!! + value
                } ?: rxWifiByTime.put(index, value)
                rxMobileByTime[index] ?: rxMobileByTime.put(index, 0.0)

                if (rxWifiByTime[index]!! > maxValue) {
                    maxValue = rxWifiByTime[index]!!.toLong()
                }
            }
            NetworkStatsMapsType.WIFI_TIME_TX -> {
                txWifiByTime[index]?.let {
                    txWifiByTime[index] = txWifiByTime[index]!! + value
                } ?: txWifiByTime.put(index, value)
                txMobileByTime[index] ?: txMobileByTime.put(index, 0.0)

                if (txWifiByTime[index]!! > maxValue) {
                    maxValue = txWifiByTime[index]!!.toLong()
                }
            }
            NetworkStatsMapsType.MOBILE_TIME_RX -> {
                rxMobileByTime[index]?.let {
                    rxMobileByTime[index] = rxMobileByTime[index]!! + value
                } ?: rxMobileByTime.put(index, value)
                rxWifiByTime[index] ?: rxWifiByTime.put(index, 0.0)

                if (rxMobileByTime[index]!! > maxValue) {
                    maxValue = rxMobileByTime[index]!!.toLong()
                }
            }
            NetworkStatsMapsType.MOBILE_TIME_TX -> {
                txMobileByTime[index]?.let {
                    txMobileByTime[index] = txMobileByTime[index]!! + value
                } ?: txMobileByTime.put(index, value)
                txWifiByTime[index] ?: txWifiByTime.put(index, 0.0)

                if (txMobileByTime[index]!! > maxValue) {
                    maxValue = txMobileByTime[index]!!.toLong()
                }
            }
            else -> {
            }
        }
    }

    private fun put(type: NetworkStatsMapsType, packageManager: PackageManager, bucket: NetworkStats.Bucket) {
        when (type) {
            NetworkStatsMapsType.WIFI_APP -> {
                if (rxWifiByApp.containsKey(bucket.uid)) {
                    rxWifiByApp[bucket.uid]!!.valueDownload = rxWifiByApp[bucket.uid]!!.valueDownload + bucket.rxBytes
                    rxWifiByApp[bucket.uid]!!.valueUpload = rxWifiByApp[bucket.uid]!!.valueUpload + bucket.txBytes
                } else {
                    try {
                        val applicationInfo = packageManager.getApplicationInfo(packageManager.getNameForUid(bucket.uid), PackageManager.GET_META_DATA)
                        var icon: Drawable? = null
                        try {
                            icon = packageManager.getApplicationIcon(applicationInfo)
                        } catch (ex2: PackageManager.NameNotFoundException) {
                        }
                        val wifiNetworkUsage = NetworkStats()
                        wifiNetworkUsage.uid = bucket.uid
                        wifiNetworkUsage.name = packageManager.getApplicationLabel(applicationInfo).toString()
                        wifiNetworkUsage.packageName = applicationInfo.packageName
                        wifiNetworkUsage.icon = icon
                        wifiNetworkUsage.valueDownload = bucket.rxBytes
                        wifiNetworkUsage.valueUpload = bucket.txBytes
                        rxWifiByApp.put(bucket.uid, wifiNetworkUsage)
                    } catch (ex: PackageManager.NameNotFoundException) {
                        val name = packageManager.getNameForUid(bucket.uid)?: "Unknown"
                        val wifiNetworkUsage = NetworkStats()
                        wifiNetworkUsage.uid = bucket.uid
                        wifiNetworkUsage.name = name
                        wifiNetworkUsage.packageName = name
                        wifiNetworkUsage.icon = null
                        wifiNetworkUsage.valueDownload = bucket.rxBytes
                        wifiNetworkUsage.valueUpload = bucket.txBytes
                        rxWifiByApp.put(bucket.uid, wifiNetworkUsage)
                    }
                }
            }
            NetworkStatsMapsType.MOBILE_APP -> {
                if (rxMobileByApp.containsKey(bucket.uid)) {
                    rxMobileByApp[bucket.uid]!!.valueDownload += bucket.rxBytes
                    rxMobileByApp[bucket.uid]!!.valueUpload += bucket.txBytes
                } else {
                    try {
                        val applicationInfo = packageManager.getApplicationInfo(packageManager.getNameForUid(bucket.uid), 0)
                        var icon: Drawable? = null
                        try {
                            icon = packageManager.getApplicationIcon(applicationInfo)
                        } catch (ex2: PackageManager.NameNotFoundException) {
                        }
                        val mobileNetworkUsage = NetworkStats()
                        mobileNetworkUsage.uid = bucket.uid
                        mobileNetworkUsage.name = packageManager.getApplicationLabel(applicationInfo).toString()
                        mobileNetworkUsage.packageName = applicationInfo.packageName
                        mobileNetworkUsage.icon = icon
                        mobileNetworkUsage.valueDownload = bucket.rxBytes
                        mobileNetworkUsage.valueUpload = bucket.txBytes
                        rxMobileByApp.put(bucket.uid, mobileNetworkUsage)
                    } catch (ex: PackageManager.NameNotFoundException) {
                        val name = packageManager.getNameForUid(bucket.uid)?: "Unknown"
                        val mobileNetworkUsage = NetworkStats()
                        mobileNetworkUsage.uid = bucket.uid
                        mobileNetworkUsage.name = name
                        mobileNetworkUsage.packageName = name
                        mobileNetworkUsage.icon = null
                        mobileNetworkUsage.valueDownload = bucket.rxBytes
                        mobileNetworkUsage.valueUpload = bucket.txBytes
                        rxMobileByApp.put(bucket.uid, mobileNetworkUsage)
                    }
                }
            }
            else -> {
            }
        }
    }
}