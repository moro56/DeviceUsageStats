package it.emperor.deviceusagestats.extensions

import android.content.res.Resources
import it.emperor.deviceusagestats.App
import it.emperor.deviceusagestats.R

val Float.dp: Float
    get() = (this / Resources.getSystem().displayMetrics.density)

val Float.px: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

fun Float.formatBytes(): String {
    val tera: Double = this.div(Math.pow(2.0, 40.0))
    val giga: Double = this.div(Math.pow(2.0, 30.0))
    val mega: Double = this.div(Math.pow(2.0, 20.0))
    val kilo: Double = this.div(Math.pow(2.0, 10.0))

    when {
        tera > 1.0 -> {
            return App.context().getString(R.string.network_value_format_no_point).format(tera) + " " +
                    App.context().getString(R.string.unit_tera)
        }
        giga > 1.0 -> {
            return App.context().getString(R.string.network_value_format_no_point).format(giga) + " " +
                    App.context().getString(R.string.unit_giga)
        }
        mega > 1.0 -> {
            return App.context().getString(R.string.network_value_format_no_point).format(mega) + " " +
                    App.context().getString(R.string.unit_mega)
        }
        else -> {
            return App.context().getString(R.string.network_value_format_no_point).format(kilo) + " " +
                    App.context().getString(R.string.unit_kilo)
        }
    }
}

fun Float.formatBytesWithDecimal(): String {
    val tera: Double = this.div(Math.pow(2.0, 40.0))
    val giga: Double = this.div(Math.pow(2.0, 30.0))
    val mega: Double = this.div(Math.pow(2.0, 20.0))
    val kilo: Double = this.div(Math.pow(2.0, 10.0))

    when {
        tera > 1.0 -> {
            return App.context().getString(R.string.network_value_format).format(tera) + " " +
                    App.context().getString(R.string.unit_tera)
        }
        giga > 1.0 -> {
            return App.context().getString(R.string.network_value_format).format(giga) + " " +
                    App.context().getString(R.string.unit_giga)
        }
        mega > 1.0 -> {
            return App.context().getString(R.string.network_value_format).format(mega) + " " +
                    App.context().getString(R.string.unit_mega)
        }
        else -> {
            return App.context().getString(R.string.network_value_format).format(kilo) + " " +
                    App.context().getString(R.string.unit_kilo)
        }
    }
}