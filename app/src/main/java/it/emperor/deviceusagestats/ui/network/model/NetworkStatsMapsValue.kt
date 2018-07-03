package it.emperor.deviceusagestats.ui.network.model

import it.emperor.deviceusagestats.App
import it.emperor.deviceusagestats.R

data class NetworkStatsMapsValue(var value: Float) {
    constructor() : this(0f)

    fun formatValue(value: Long?): String {
        val tera: Double = value?.div(Math.pow(2.0, 40.0)) ?: 0.0
        val giga: Double = value?.div(Math.pow(2.0, 30.0)) ?: 0.0
        val mega: Double = value?.div(Math.pow(2.0, 20.0)) ?: 0.0
        val kilo: Double = value?.div(Math.pow(2.0, 10.0)) ?: 0.0

        if (tera.compareTo(1.0) > 0) {
            return "${App.context().getString(R.string.network_value_format).format(tera)} ${App.context().getString(R.string.network_unit_tera)}"
        } else if (giga.compareTo(1.0) > 0) {
            return "${App.context().getString(R.string.network_value_format).format(giga)} ${App.context().getString(R.string.network_unit_giga)}"
        } else if (mega.compareTo(1.0) > 0) {
            return "${App.context().getString(R.string.network_value_format).format(mega)} ${App.context().getString(R.string.network_unit_mega)}"
        } else {
            return "${App.context().getString(R.string.network_value_format).format(kilo)} ${App.context().getString(R.string.network_unit_kilo)}"
        }
    }
}