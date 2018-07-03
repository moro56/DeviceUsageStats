package it.emperor.deviceusagestats.events

import android.widget.ImageView
import android.widget.TextView

data class AppDetailEvent(val packageName: String, val iconView: ImageView, val nameView: TextView)