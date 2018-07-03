package it.emperor.deviceusagestats.extensions

fun Long.toTimeString(): String {
    val days = (this / (24 * 60 * 60 * 1000)) % 60
    val hours = (this / (60 * 60 * 1000)) % 60
    val minutes = (this / (60 * 1000)) % 60
    val seconds = (this / 1000) % 60

    var timeString = ""
    if (days != 0L) {
        timeString = timeString.plus("${days}d ")
    }
    if (hours != 0L) {
        timeString = timeString.plus("${hours}h ")
    }
    if (minutes != 0L) {
        timeString = timeString.plus("${minutes}m ")
    }
    timeString = timeString.plus("${seconds}s ")

    return timeString
}