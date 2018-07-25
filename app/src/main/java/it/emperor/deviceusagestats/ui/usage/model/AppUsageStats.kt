package it.emperor.deviceusagestats.ui.usage.model

import android.graphics.drawable.Drawable

data class AppUsageStats(var packageName: String, var timeInForeground: Long, var lastUsedTime: Long, var name: String, var icon: Drawable?) {
    constructor(packageName: String, timeInForeground: Long, lastUsedTime: Long) : this(packageName, timeInForeground, lastUsedTime, "", null)
}