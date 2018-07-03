package it.emperor.deviceusagestats.ui.usage.model

import android.graphics.drawable.Drawable

data class AppUsageStats(var packageName: String, var usedTime: Long, var timeInForeground: Long, var lastUsedTime: Long, var name: String?, var icon: Drawable?) {
    constructor(packageName: String, usedTime: Long, timeInForeground: Long, lastUsedTime: Long) : this(packageName, usedTime, timeInForeground, lastUsedTime, null, null)
}