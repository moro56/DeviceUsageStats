package it.emperor.deviceusagestats.ui.usage.model

import android.app.usage.UsageStats
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class AppUsageStatsMaps(var foregroundUsage: MutableList<AppUsageStats>, var totalUsage: MutableMap<String, AppUsageStats>) {
    constructor() : this(mutableListOf(), mutableMapOf())

    internal var foregroundUsageTotal: Long = 0
    internal var totalUsageTotal: Long = 0

    internal var totalUsageList: MutableList<AppUsageStats> = mutableListOf()

    fun init(packageManager: PackageManager, usages: MutableMap<String, UsageStats>?, usageDetails: MutableList<UsageStats>?) {
        foregroundUsageTotal = 0
        totalUsageTotal = 0

        foregroundUsage.clear()
        totalUsage.clear()
        totalUsageList.clear()

        if (usages != null) {
            for ((key, value) in usages) {
                put(packageManager, key, value)
            }
        }

        foregroundUsage.sortByDescending { it.timeInForeground }
        foregroundUsage = foregroundUsage.filter { it.timeInForeground.toFloat() / foregroundUsageTotal.toFloat() >= 0.001f }.toMutableList()

        if (usageDetails != null) {
            for (usageDetail in usageDetails) {
                put(packageManager, usageDetail)
            }
        }

        for ((key, value) in totalUsage) {
            totalUsageTotal += value.usedTime
            totalUsageList.add(value)
        }

        totalUsageList.sortByDescending { it.usedTime }
        totalUsageList = totalUsageList.filter { it.usedTime.toFloat() / totalUsageTotal.toFloat() >= 0.001f }.toMutableList()
    }

    private fun put(packageManager: PackageManager, key: String, value: UsageStats) {
        val usageStats = AppUsageStats(key, 0, value.totalTimeInForeground, value.lastTimeUsed)
        try {
            val applicationInfo = packageManager.getApplicationInfo(key, PackageManager.GET_META_DATA)
            var icon: Drawable? = null
            try {
                icon = packageManager.getApplicationIcon(applicationInfo)
            } catch (ex2: PackageManager.NameNotFoundException) {
            }
            usageStats.name = packageManager.getApplicationLabel(applicationInfo).toString()
            usageStats.icon = icon
        } catch (ex: PackageManager.NameNotFoundException) {
            usageStats.name = key
            usageStats.icon = null
        }
        foregroundUsage.add(usageStats)
        foregroundUsageTotal += usageStats.timeInForeground
    }

    private fun put(packageManager: PackageManager, usage: UsageStats) {
        if (totalUsage.containsKey(usage.packageName)) {
            totalUsage[usage.packageName]!!.timeInForeground = totalUsage[usage.packageName]!!.timeInForeground + usage.totalTimeInForeground
            totalUsage[usage.packageName]!!.usedTime = totalUsage[usage.packageName]!!.usedTime + (usage.lastTimeStamp - usage.firstTimeStamp)
        } else {
            val usageStats = AppUsageStats(usage.packageName, usage.lastTimeStamp - usage.firstTimeStamp, usage.totalTimeInForeground, usage.lastTimeUsed)
            try {
                val applicationInfo = packageManager.getApplicationInfo(usage.packageName, PackageManager.GET_META_DATA)
                var icon: Drawable? = null
                try {
                    icon = packageManager.getApplicationIcon(applicationInfo)
                } catch (ex2: PackageManager.NameNotFoundException) {
                }

                usageStats.name = packageManager.getApplicationLabel(applicationInfo).toString()
                usageStats.icon = icon
            } catch (ex: PackageManager.NameNotFoundException) {
                usageStats.name = usage.packageName
                usageStats.icon = null
            }
            totalUsage.put(usage.packageName, usageStats)
        }
    }
}