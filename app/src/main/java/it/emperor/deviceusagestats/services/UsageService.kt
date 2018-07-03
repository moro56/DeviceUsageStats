package it.emperor.deviceusagestats.services

import android.annotation.SuppressLint
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import it.emperor.deviceusagestats.App
import org.joda.time.DateTime

class UsageService {

    companion object {
        val WIFI = ConnectivityManager.TYPE_WIFI
        val MOBILE = ConnectivityManager.TYPE_MOBILE
    }

    private var usageStatsManager: UsageStatsManager
    private var networkStatsManager: NetworkStatsManager
    private var telephonyManager: TelephonyManager

    init {
        usageStatsManager = App.context().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        networkStatsManager = App.context().getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        telephonyManager = App.context().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    fun loadNetworkSummaryStats(type: Int, start: DateTime, end: DateTime): NetworkStats.Bucket? {
        val subscriberId = telephonyManager.subscriberId ?: ""
        return networkStatsManager.querySummaryForUser(type, subscriberId, start.toDate().time, end.toDate().time)
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    fun loadNetworkDetailStats(type: Int, start: DateTime, end: DateTime): NetworkStats? {
        val subscriberId = telephonyManager.subscriberId ?: ""
        return networkStatsManager.queryDetails(type, subscriberId, start.toDate().time, end.toDate().time)
    }

    fun loadUsageSummaryStats(start: DateTime, end: DateTime): MutableMap<String, UsageStats>? {
        return usageStatsManager.queryAndAggregateUsageStats(start.toDate().time, end.toDate().time);
    }

    fun loadUsageStats(start: DateTime, end: DateTime): MutableList<UsageStats>? {
        return usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, start.toDate().time, end.toDate().time);
    }
}