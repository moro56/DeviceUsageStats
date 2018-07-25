package it.emperor.deviceusagestats.ui.usage.model

import android.app.usage.UsageStats
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class AppUsageStatsMaps(val packageManager: PackageManager) {

    internal val totalUsage: MutableMap<String, AppUsageStats> = mutableMapOf()

    internal var total: Long = 0
    internal var totalForeground: Long = 0

    fun update(usageStats: MutableList<UsageStats>?, usageStatsSummary: MutableMap<String, UsageStats>?) {
        totalUsage.clear()

        total = 0
        totalForeground = 0

        if (usageStatsSummary != null) {
            for ((key, value) in usageStatsSummary) {
                println(key + " + " + value)
            }
        }

        if (usageStats != null) {
            for (usageDetail in usageStats) {
                addValue(usageDetail)
            }
        }
    }

    private fun addValue(usage: UsageStats) {
        val totalTime = usage.lastTimeStamp - usage.firstTimeStamp
        if (totalUsage.containsKey(usage.packageName)) {
            totalUsage[usage.packageName]!!.timeInForeground = totalUsage[usage.packageName]!!.timeInForeground + usage.totalTimeInForeground
        } else {
            val usageStats = AppUsageStats(usage.packageName, usage.totalTimeInForeground, usage.lastTimeUsed)
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

        total = if (totalTime > total) totalTime else total
        totalForeground += usage.totalTimeInForeground
    }
}