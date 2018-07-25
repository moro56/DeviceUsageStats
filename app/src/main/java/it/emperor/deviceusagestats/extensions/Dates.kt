package it.emperor.deviceusagestats.extensions

fun Long.toTimeString(limit: Boolean = false): String {
    val days = (this / (24 * 60 * 60 * 1000)) % 60
    val hours = (this / (60 * 60 * 1000)) % 60
    val minutes = (this / (60 * 1000)) % 60
    val seconds = (this / 1000) % 60

    var count = 0
    var timeString = ""
    if (days != 0L) {
        timeString = timeString.plus("${days}d ")
        count++
    }
    if (hours != 0L) {
        timeString = timeString.plus("${hours}h ")
        count++
    }
    if (minutes != 0L) {
        if (count < 2 && limit) {
            timeString = timeString.plus("${minutes}m ")
        }
        count++
    }
    if (count < 2 && limit) {
        timeString = timeString.plus("${seconds}s ")
    }

    return timeString
}

fun Long.toTimeStringArray(): List<String> {
    val days = (this / (24 * 60 * 60 * 1000)) % 60
    val hours = (this / (60 * 60 * 1000)) % 60
    val minutes = (this / (60 * 1000)) % 60
    val seconds = (this / 1000) % 60

    val list = mutableListOf<String>()

    if (days != 0L) {
        list.add(days.toString())
        list.add("d")
    }
    if (hours != 0L) {
        list.add(hours.toString())
        list.add("h")
    }
    if (minutes != 0L) {
        list.add(minutes.toString())
        list.add("m")
    }
    list.add(seconds.toString())
    list.add("s")

    return list
}