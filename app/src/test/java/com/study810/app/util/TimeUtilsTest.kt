package com.study810.app.util

import com.study810.app.data.StudySession
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeUtilsTest {

    @Test
    fun `formatTimer shows full target`() {
        assertEquals("810:00:00", formatTimer(TARGET_MILLIS))
    }

    @Test
    fun `formatTimer handles zero and sub-second values`() {
        assertEquals("000:00:00", formatTimer(0L))
        assertEquals("000:00:00", formatTimer(999L))
        assertEquals("001:01:01", formatTimer(3_661_000L))
    }

    @Test
    fun `formatDuration uses hours and minutes`() {
        assertEquals("0分", formatDuration(0L))
        assertEquals("30秒", formatDuration(30_000L))
        assertEquals("45分", formatDuration(45L * 60_000L))
        assertEquals("1時間", formatDuration(3_600_000L))
        assertEquals("1時間1分", formatDuration(3_661_000L))
    }

    @Test
    fun `compact duration marks sub-minute records`() {
        assertEquals("<1m", formatCompactDuration(30_000L))
        assertEquals("45m", formatCompactDuration(45L * 60_000L))
        assertEquals("2h05", formatCompactDuration(125L * 60_000L))
    }

    @Test
    fun `sessions are split at midnight`() {
        val zone = ZoneId.of("Asia/Tokyo")
        val start = LocalDateTime.of(2026, 11, 7, 23, 30).atZone(zone).toInstant().toEpochMilli()
        val end = LocalDateTime.of(2026, 11, 8, 0, 30).atZone(zone).toInstant().toEpochMilli()
        val session = StudySession(
            startEpochMillis = start,
            endEpochMillis = end,
            durationMillis = end - start,
        )

        val records = buildDailyRecords(listOf(session), null, end, zone)

        assertEquals(30L * 60_000L, records.getValue(LocalDate.of(2026, 11, 7)).totalMillis)
        assertEquals(30L * 60_000L, records.getValue(LocalDate.of(2026, 11, 8)).totalMillis)
    }

    @Test
    fun `running session contributes up to now`() {
        val zone = ZoneId.of("Asia/Tokyo")
        val start = LocalDateTime.of(2026, 11, 7, 9, 0).atZone(zone).toInstant().toEpochMilli()
        val now = start + 90L * 60_000L

        val records = buildDailyRecords(emptyList(), start, now, zone)

        val record = records.getValue(LocalDate.of(2026, 11, 7))
        assertEquals(90L * 60_000L, record.totalMillis)
        assertTrue(record.entries.single().running)
    }
}
