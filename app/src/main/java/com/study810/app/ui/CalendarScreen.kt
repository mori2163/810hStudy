package com.study810.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.study810.app.R
import com.study810.app.data.StudySession
import com.study810.app.util.DayRecord
import com.study810.app.util.SessionEntry
import com.study810.app.util.buildDailyRecords
import com.study810.app.util.formatClock
import com.study810.app.util.formatCompactDuration
import com.study810.app.util.formatDateJapanese
import com.study810.app.util.formatDuration
import com.study810.app.util.formatMonth
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    sessions: List<StudySession>,
    runningStartEpochMillis: Long?,
) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(runningStartEpochMillis) {
        while (runningStartEpochMillis != null) {
            nowMillis = System.currentTimeMillis()
            delay(15_000L)
        }
    }

    val records = remember(sessions, runningStartEpochMillis, nowMillis / 60_000L) {
        buildDailyRecords(
            sessions = sessions,
            runningStartMillis = runningStartEpochMillis,
            nowMillis = nowMillis,
        )
    }

    var monthText by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    val month = YearMonth.parse(monthText)
    var selectedEpochDay by rememberSaveable { mutableLongStateOf(-1L) }
    val today = LocalDate.now()

    val monthRecords = remember(records, month) {
        records.filterKeys { YearMonth.from(it) == month }
    }
    val monthTotal = monthRecords.values.sumOf { it.totalMillis }
    val recordedDays = monthRecords.count { it.value.totalMillis > 0L }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 112.dp),
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = formatMonth(month),
                color = Paper,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { monthText = month.minusMonths(1).toString() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.previous_month),
                    tint = Paper.copy(alpha = 0.6f),
                )
            }
            IconButton(onClick = { monthText = month.plusMonths(1).toString() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.next_month),
                    tint = Paper.copy(alpha = 0.6f),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        MonthSummary(
            month = month,
            totalMillis = monthTotal,
            recordedDays = recordedDays,
        )

        Spacer(modifier = Modifier.height(8.dp))
        CalendarGrid(
            month = month,
            records = records,
            today = today,
            selectedEpochDay = selectedEpochDay,
            onSelectDay = { selectedEpochDay = it.toEpochDay() },
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (selectedEpochDay >= 0L) {
        val date = LocalDate.ofEpochDay(selectedEpochDay)
        val record = records[date]
        DayDetailSheet(
            date = date,
            record = record,
            onDismiss = { selectedEpochDay = -1L },
        )
    }
}

@Composable
private fun MonthSummary(
    month: YearMonth,
    totalMillis: Long,
    recordedDays: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.month_summary_title, formatMonth(month)),
                color = Paper.copy(alpha = 0.5f),
                fontSize = 11.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatDuration(totalMillis),
                color = Paper,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = stringResource(R.string.days_recorded, recordedDays),
            color = Paper.copy(alpha = 0.5f),
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 4.dp),
        )
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    records: Map<LocalDate, DayRecord>,
    today: LocalDate,
    selectedEpochDay: Long,
    onSelectDay: (LocalDate) -> Unit,
) {
    val weekdays = stringArrayResource(R.array.weekdays_short)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdays.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Paper.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        val leadingEmpty = month.atDay(1).dayOfWeek.value % 7
        val dayCount = month.lengthOfMonth()
        val rowCount = (leadingEmpty + dayCount + 6) / 7

        repeat(rowCount) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { column ->
                    val dayIndex = row * 7 + column - leadingEmpty
                    if (dayIndex in 0 until dayCount) {
                        val date = month.atDay(dayIndex + 1)
                        val record = records[date]
                        DayCell(
                            date = date,
                            record = record,
                            isToday = date == today,
                            isSelected = date.toEpochDay() == selectedEpochDay,
                            onClick = { onSelectDay(date) },
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.DayCell(
    date: LocalDate,
    record: DayRecord?,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(2.dp)
    val intensity = intensityFor(record)

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(0.82f)
            .padding(1.5.dp)
            .clip(shape)
            .background(
                if (intensity > 0f) Paper.copy(alpha = intensity) else Color.Transparent,
            )
            .then(
                when {
                    isSelected -> Modifier.border(1.dp, Paper, shape)
                    isToday -> Modifier.border(1.dp, Paper.copy(alpha = 0.35f), shape)
                    else -> Modifier
                },
            )
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = Paper,
                fontSize = 12.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (record != null && record.totalMillis > 0L) {
                Text(
                    text = formatCompactDuration(record.totalMillis),
                    color = Paper.copy(alpha = 0.6f),
                    fontSize = 8.sp,
                    maxLines = 1,
                )
            }
        }
    }
}

private fun intensityFor(record: DayRecord?): Float {
    val minutes = (record?.totalMillis ?: 0L) / 60_000L
    return when {
        minutes <= 0L -> 0f
        minutes < 30L -> 0.06f
        minutes < 60L -> 0.10f
        minutes < 120L -> 0.15f
        minutes < 240L -> 0.21f
        else -> 0.28f
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDetailSheet(
    date: LocalDate,
    record: DayRecord?,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        contentColor = Paper,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Paper.copy(alpha = 0.2f))
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = formatDateJapanese(date),
                color = Paper,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (record == null || record.totalMillis <= 0L) {
                    stringResource(R.string.no_record_short)
                } else {
                    stringResource(R.string.day_total, formatDuration(record.totalMillis))
                },
                color = Paper.copy(alpha = 0.5f),
                fontSize = 12.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Paper.copy(alpha = 0.12f))

            val entries = record?.entries.orEmpty()
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.no_record),
                        color = Paper.copy(alpha = 0.4f),
                        fontSize = 13.sp,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 380.dp),
                ) {
                    items(entries) { entry ->
                        SessionRow(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionRow(entry: SessionEntry) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${formatClock(entry.startMillis)} - " +
                    if (entry.running) "…" else formatClock(entry.endMillis),
                color = Paper,
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formatDuration(entry.endMillis - entry.startMillis),
                color = Paper.copy(alpha = 0.7f),
                fontSize = 13.sp,
            )
        }
        HorizontalDivider(color = Paper.copy(alpha = 0.08f))
    }
}
