package com.study810.app.util

import com.study810.app.data.StudySession
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale

/** The challenge target: 810 hours. */
const val TARGET_MILLIS: Long = 810L * 60L * 60L * 1_000L

private val weekdayLabels = arrayOf("日", "月", "火", "水", "木", "金", "土")

/** "810:00:00" style countdown. Hours are always at least three digits. */
fun formatTimer(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0L) / 1_000L
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.US, "%03d:%02d:%02d", hours, minutes, seconds)
}

/** "1時間23分" style, rounded down to the minute. Sub-minute values show seconds. */
fun formatDuration(ms: Long): String {
    val safeMs = ms.coerceAtLeast(0L)
    val totalMinutes = safeMs / 60_000L
    if (totalMinutes == 0L) {
        val seconds = safeMs / 1_000L
        return if (seconds > 0L) "${seconds}秒" else "0分"
    }
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return when {
        hours > 0L && minutes > 0L -> "${hours}時間${minutes}分"
        hours > 0L -> "${hours}時間"
        else -> "${minutes}分"
    }
}

/** Compact label for the small calendar cells, e.g. "45m", "2h05", "<1m". */
fun formatCompactDuration(ms: Long): String {
    val totalMinutes = ms.coerceAtLeast(0L) / 60_000L
    if (totalMinutes == 0L) return "<1m"
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return if (hours > 0L) {
        String.format(Locale.US, "%dh%02d", hours, minutes)
    } else {
        "${minutes}m"
    }
}

/** "09:05" in the device time zone. */
fun formatClock(
    epochMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String {
    val time = Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalTime()
    return String.format(Locale.US, "%02d:%02d", time.hour, time.minute)
}

fun weekdayLabel(date: LocalDate): String = weekdayLabels[date.dayOfWeek.value % 7]

fun formatDateJapanese(date: LocalDate): String =
    "${date.monthValue}月${date.dayOfMonth}日（${weekdayLabel(date)}）"

fun formatDateDots(date: LocalDate): String =
    "${date.year}.${date.monthValue.toString().padStart(2, '0')}.${date.dayOfMonth.toString().padStart(2, '0')}"

fun formatMonth(month: YearMonth): String = "${month.year}年${month.monthValue}月"

fun overlapMillis(start: Long, end: Long, rangeStart: Long, rangeEnd: Long): Long =
    (minOf(end, rangeEnd) - maxOf(start, rangeStart)).coerceAtLeast(0L)

data class SessionEntry(
    val startMillis: Long,
    val endMillis: Long,
    val running: Boolean,
)

data class DayRecord(
    val totalMillis: Long,
    val entries: List<SessionEntry>,
)

/**
 * Splits sessions on local-day boundaries so a session that runs past midnight
 * contributes to both days. A running session is included up to [nowMillis].
 */
fun buildDailyRecords(
    sessions: List<StudySession>,
    runningStartMillis: Long?,
    nowMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault(),
): Map<LocalDate, DayRecord> {
    val buckets = mutableMapOf<LocalDate, MutableList<SessionEntry>>()

    fun addInterval(start: Long, end: Long, running: Boolean) {
        if (end <= start) return
        val startDate = Instant.ofEpochMilli(start).atZone(zoneId).toLocalDate()
        val endDate = Instant.ofEpochMilli(end).atZone(zoneId).toLocalDate()
        var date = startDate
        while (!date.isAfter(endDate)) {
            val dayStart = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val dayEnd = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val clippedStart = maxOf(start, dayStart)
            val clippedEnd = minOf(end, dayEnd)
            if (clippedEnd > clippedStart) {
                buckets.getOrPut(date) { mutableListOf() }
                    .add(SessionEntry(clippedStart, clippedEnd, running))
            }
            date = date.plusDays(1)
        }
    }

    sessions.forEach { addInterval(it.startEpochMillis, it.endEpochMillis, false) }
    if (runningStartMillis != null && nowMillis > runningStartMillis) {
        addInterval(runningStartMillis, nowMillis, true)
    }

    return buckets.mapValues { (_, entries) ->
        DayRecord(
            totalMillis = entries.sumOf { it.endMillis - it.startMillis },
            entries = entries.sortedBy { it.startMillis },
        )
    }
}
