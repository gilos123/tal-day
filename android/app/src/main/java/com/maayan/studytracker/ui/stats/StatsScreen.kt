package com.maayan.studytracker.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stats") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            !state.hasAnyData -> EmptyState(Modifier.padding(padding))
            else -> StatsContent(state, Modifier.padding(padding))
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "No study sessions yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Start a timer from your schedule to see your stats here.",
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun StatsContent(state: StatsUiState, modifier: Modifier) {
    val modelProducer = remember { ChartEntryModelProducer() }
    val weekModelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(state.dailyLast14) {
        val entries = state.dailyLast14.mapIndexed { idx, bar ->
            entryOf(idx.toFloat(), bar.minutes.toFloat())
        }
        modelProducer.setEntries(entries)
    }
    LaunchedEffect(state.currentWeekBars) {
        val entries = state.currentWeekBars.mapIndexed { idx, bar ->
            entryOf(idx.toFloat(), bar.minutes.toFloat())
        }
        weekModelProducer.setEntries(entries)
    }

    val barDates: List<LocalDate> = state.dailyLast14.map { it.date }
    val weekDates: List<LocalDate> = state.currentWeekBars.map { it.date }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HeadlineStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Today",
                    primary = formatMinutes(state.todayMinutes),
                    secondary = DateTimeFormatter.ofPattern("EEE, MMM d").format(LocalDate.now())
                )
                HeadlineStatCard(
                    modifier = Modifier.weight(1f),
                    label = "This week",
                    primary = formatMinutes(state.thisWeekTotalMinutes),
                    secondary = "${formatMinutes(state.thisWeekAveragePerElapsedDay)}/day " +
                            "(over ${state.thisWeekDaysElapsed} day${if (state.thisWeekDaysElapsed == 1) "" else "s"})"
                )
                HeadlineStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Streak",
                    primary = "${state.currentStreakDays}",
                    secondary = if (state.currentStreakDays == 1) "day" else "days"
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LifetimeStat(
                        label = "Total study time",
                        value = formatMinutesLong(state.totalStudyMinutes)
                    )
                    LifetimeStat(
                        label = "Sessions",
                        value = state.totalSessions.toString()
                    )
                }
            }
        }

        item { SectionHeader("This week") }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Chart(
                        chart = columnChart(),
                        chartModelProducer = weekModelProducer,
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(
                            valueFormatter = dayOfWeekFormatter(weekDates)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Minutes studied per day · " +
                                "${weekDates.firstOrNull()?.format(MONTH_DAY).orEmpty()} – " +
                                "${weekDates.lastOrNull()?.format(MONTH_DAY).orEmpty()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        item { SectionHeader("Last 14 days") }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Chart(
                        chart = columnChart(),
                        chartModelProducer = modelProducer,
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(
                            valueFormatter = dayOfMonthFormatter(barDates)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Minutes studied per day · " +
                                "${barDates.firstOrNull()?.format(SHORT_MD).orEmpty()} – " +
                                "${barDates.lastOrNull()?.format(SHORT_MD).orEmpty()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        item { SectionHeader("Previous weeks") }
        items(state.previousWeeks, key = { it.weekStart.toString() }) { w -> PreviousWeekRow(w) }
    }
}

@Composable
private fun HeadlineStatCard(
    modifier: Modifier,
    label: String,
    primary: String,
    secondary: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                label.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(6.dp))
            Text(primary, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(
                secondary,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun LifetimeStat(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun PreviousWeekRow(w: WeekAverage) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    formatWeekRange(w.weekStart, w.weekEnd),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${formatMinutes(w.averageMinutesPerDay)} / day",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Text(
                formatMinutes(w.totalMinutes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private val SHORT_MD: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d")
private val MONTH_DAY: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")
private val MONTH_DAY_YEAR: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

private fun formatMinutes(mins: Double): String {
    if (mins <= 0.0) return "0m"
    val total = mins.toInt()
    val h = total / 60
    val m = total % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

private fun formatMinutesLong(mins: Double): String {
    if (mins <= 0.0) return "0m"
    val total = mins.toInt()
    val h = total / 60
    val m = total % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

private fun formatWeekRange(start: LocalDate, end: LocalDate): String {
    val today = LocalDate.now()
    val sameYearAsToday = start.year == today.year && end.year == today.year
    return if (sameYearAsToday) {
        "${start.format(MONTH_DAY)} – ${end.format(MONTH_DAY)}"
    } else {
        "${start.format(MONTH_DAY)} – ${end.format(MONTH_DAY_YEAR)}"
    }
}

private fun dayOfMonthFormatter(
    dates: List<LocalDate>
): AxisValueFormatter<AxisPosition.Horizontal.Bottom> =
    AxisValueFormatter { value, _ ->
        val idx = value.toInt()
        if (idx !in dates.indices) return@AxisValueFormatter ""
        if (idx % 2 != 0 && dates.size > 8) return@AxisValueFormatter ""
        dates[idx].dayOfMonth.toString()
    }

private fun dayOfWeekFormatter(
    dates: List<LocalDate>
): AxisValueFormatter<AxisPosition.Horizontal.Bottom> =
    AxisValueFormatter { value, _ ->
        val idx = value.toInt()
        if (idx !in dates.indices) return@AxisValueFormatter ""
        dates[idx].dayOfWeek.getDisplayName(
            java.time.format.TextStyle.NARROW,
            java.util.Locale.getDefault()
        )
    }
