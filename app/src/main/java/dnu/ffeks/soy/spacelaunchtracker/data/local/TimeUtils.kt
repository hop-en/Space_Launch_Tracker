package dnu.ffeks.soy.spacelaunchtracker.data.local

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun formatToLocalTime(utcTimeString: String?): String {
    if (utcTimeString == null) return "Unknown time"
    return try {
        val instant = Instant.parse(utcTimeString)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())

        formatter.format(instant)
    } catch (e: Exception) {
        utcTimeString
    }
}
fun calculateTimeRemaining(netDateString: String): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")

        val targetDate = format.parse(netDateString) ?: return "00 : 00 : 00 : 00"
        val diff = targetDate.time - System.currentTimeMillis()

        if (diff <= 0) return "Launched!"

        val days = diff / (1000 * 60 * 60 * 24)
        val hours = (diff / (1000 * 60 * 60)) % 24
        val mins = (diff / (1000 * 60)) % 60
        val secs = (diff / 1000) % 60

        String.format(Locale.US, "%02d : %02d : %02d : %02d", days, hours, mins, secs)

    } catch (e: Exception) {
        "00 : 00 : 00 : 00"
    }
}