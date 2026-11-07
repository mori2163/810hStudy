package com.study810.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.study810.app.R
import com.study810.app.util.TARGET_MILLIS
import com.study810.app.util.formatDateDots
import com.study810.app.util.formatDuration
import com.study810.app.util.formatTimer
import java.time.LocalDate

@Composable
fun TimerScreen(
    state: StudyUiState,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    var showStopDialog by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.sofa_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.9f),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 14.dp, bottom = 112.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.remaining_time),
                color = Paper.copy(alpha = 0.7f),
                fontSize = 12.sp,
                letterSpacing = 2.sp,
            )
            Spacer(modifier = Modifier.height(10.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val timerFontSize = (maxWidth.value / 5.7f).coerceIn(34f, 68f).sp
                Text(
                    text = formatTimer(state.remainingMillis),
                    color = Paper,
                    fontSize = timerFontSize,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(
                    R.string.consumed_of_target,
                    formatDuration(state.consumedMillis),
                ),
                color = Paper.copy(alpha = 0.6f),
                fontSize = 12.sp,
            )

            Spacer(modifier = Modifier.height(22.dp))
            ChallengeProgress(
                fraction = (state.consumedMillis.toFloat() / TARGET_MILLIS.toFloat())
                    .coerceIn(0f, 1f),
            )

            Spacer(modifier = Modifier.height(28.dp))
            TodayRow(todayMillis = state.todayMillis)

            Spacer(modifier = Modifier.weight(1f))

            if (state.remainingMillis == 0L) {
                Text(
                    text = stringResource(R.string.achieved),
                    color = Paper,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            TimerButton(
                isRunning = state.isRunning,
                onClick = {
                    if (state.isRunning) showStopDialog = true else onStart()
                },
            )

            state.firstLaunchEpochDay?.let { epochDay ->
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(
                        R.string.first_launch,
                        formatDateDots(LocalDate.ofEpochDay(epochDay)),
                    ),
                    color = Paper.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                )
            }
        }
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text(text = stringResource(R.string.stop_dialog_title)) },
            text = { Text(text = stringResource(R.string.stop_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showStopDialog = false
                        onStop()
                    },
                ) {
                    Text(text = stringResource(R.string.stop_and_save), color = Paper)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) {
                    Text(text = stringResource(R.string.keep_running), color = Paper.copy(alpha = 0.6f))
                }
            },
            containerColor = Panel,
            titleContentColor = Paper,
            textContentColor = Paper.copy(alpha = 0.65f),
        )
    }
}

@Composable
private fun ChallengeProgress(fraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(Paper.copy(alpha = 0.25f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .background(Paper),
        )
    }
}

@Composable
private fun TodayRow(todayMillis: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.today_study),
            color = Paper.copy(alpha = 0.6f),
            fontSize = 12.sp,
        )
        Text(
            text = formatDuration(todayMillis),
            color = Paper,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun TimerButton(
    isRunning: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(104.dp)
            .clip(CircleShape)
            .background(if (isRunning) Paper else Color.Black.copy(alpha = 0.25f))
            .border(1.5.dp, Paper.copy(alpha = if (isRunning) 1f else 0.7f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (isRunning) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(Ink),
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Paper,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(if (isRunning) R.string.stop else R.string.start),
                color = if (isRunning) Ink else Paper,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
