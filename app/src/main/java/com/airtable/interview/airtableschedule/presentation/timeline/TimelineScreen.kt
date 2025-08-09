package com.airtable.interview.airtableschedule.presentation.timeline

import android.text.format.DateFormat
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airtable.interview.airtableschedule.models.Event
import com.airtable.interview.airtableschedule.utils.assignLanes
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlin.math.max

/**
 * TimelineScreen: composable entry shown from MainActivity.
 * Uses the existing ViewModel to obtain events and then renders swimlanes.
 */
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TimelineView(events = uiState.events)
}

/**
 * Top-level view that:
 * 1) Guards empty state
 * 2) Computes global min/max date and totalDays
 * 3) Assigns lanes using existing assignLanes(...) helper
 * 4) Renders lanes with a date scale and swimlane rows
 */
@Composable
private fun TimelineView(events: List<Event>) {
    if (events.isEmpty()) {
        // Simple empty-state
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No events to display")
        }
        return
    }

    // Compute global bounds
    val minDate = events.minOf { it.startDate }
    val maxDate = events.maxOf { it.endDate }

    // Total days inclusive for the whole timeline range
    val totalDays = daysBetweenInclusive(minDate, maxDate).toFloat().coerceAtLeast(1f)

    // Reuse provided algorithm to group events into non-overlapping lanes
    val lanes = assignLanes(events)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Render a simple date scale (labels a cada ~6 divisões para evitar poluição)
        DateScale(events = events.distinctBy { it.startDate }, minDate = minDate, maxDate = maxDate)

        Spacer(modifier = Modifier.height(8.dp))

        // Render each lane. Cada lane é scrollável horizontalmente para suportar timelines longas.
        lanes.forEachIndexed { laneIndex, laneEvents ->
            SwimlaneRow(
                laneIndex = laneIndex,
                events = laneEvents,
                minDate = minDate,
                totalDays = totalDays
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Uma escala horizontal simples de datas, exibindo um rótulo a cada N dias para legibilidade.
 */
@Composable
private fun DateScale(events: List<Event>, minDate: Date, maxDate: Date) {
    val totalDays = daysBetweenInclusive(minDate, maxDate).toInt()
    val labelInterval = max(1, totalDays / 6) // Cerca de 6 rótulos ao longo do eixo

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        events.forEach { event ->
            val dayOfTheWeek = DateFormat.format("EEE", event.startDate) as String // Thursday
            val day = DateFormat.format("dd", event.startDate) as String // 20
            val monthString = DateFormat.format("MMM", event.startDate) as String // Jun
            Column(
                modifier = Modifier.width(80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = monthString.uppercase(),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = dayOfTheWeek.uppercase(),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = day.uppercase()
                )
            }
        }
//        var prev = 0
//        for (dayIndex in 0..totalDays) {
//            if (dayIndex == 0 || dayIndex % labelInterval == 0 || dayIndex == totalDays) {
//                val label =
//                    minDate.toInstant().plus(dayIndex.toLong(), ChronoUnit.DAYS).toString().substring(0, 10)
//                val gap = (dayIndex - prev).toFloat().coerceAtLeast(0.1f)
//                Spacer(modifier = Modifier.weight(gap))
//                Text(
//                    text = label,
//                    modifier = Modifier.padding(horizontal = 4.dp),
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//                prev = dayIndex
//            }
//        }
    }
}

/**
 * Linha (lane) da timeline com eventos dispostos proporcionalmente.
 * Garante espaçamento correto e evita peso zero no Spacer.
 */
@Composable
private fun SwimlaneRow(laneIndex: Int, events: List<Event>, minDate: Date, totalDays: Float) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            //.height(64.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
            .horizontalScroll(scrollState)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val eventInfos = events.sortedBy { it.startDate }.map { event ->
            val offset = daysBetweenInclusive(minDate, event.startDate).toInt() - 1 // zero-based offset
            val duration = daysBetweenInclusive(event.startDate, event.endDate).toInt()
            EventInfo(event, offset.coerceAtLeast(0), duration.coerceAtLeast(1))
        }

        var prevEnd = 0
        Column {
            eventInfos.forEach { info ->
                val gap = (info.offset - prevEnd).toFloat().coerceAtLeast(0f).dp

                if (gap > 0.dp) {
                    Spacer(modifier = Modifier.width(gap))
                }
                EventBox(modifier = Modifier.width((info.duration * 50).dp), event = info.event)
                prevEnd = info.offset + info.duration
            }
        }

        val remaining = (totalDays - prevEnd.toFloat()).coerceAtLeast(0f)
        if (remaining > 0f) {
            Spacer(modifier = Modifier.weight(remaining))
        }
    }
}

/**
 * Caixa individual para um evento, com largura proporcional à duração.
 * Cores fixas para variar visualmente entre eventos.
 */
@Composable
private fun EventBox(modifier: Modifier = Modifier, event: Event) {
    val colors = listOf(
        0xFF7C4DFF, // roxo
        0xFF03DAC5, // teal
        0xFFFFB74D, // laranja
        0xFF90CAF9, // azul claro
        0xFFEF9A9A  // vermelho claro
    )
    val color = androidx.compose.ui.graphics.Color(colors[event.id % colors.size])

    Card(
        modifier = modifier
            .height(48.dp)
            .padding(horizontal = 4.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Largura mínima para evitar caixas muito pequenas
                .widthIn(min = 64.dp)
                .background(color)
                .padding(8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = event.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                softWrap = false
            )
        }
    }
}


private data class EventInfo(val event: Event, val offset: Int, val duration: Int)

/** Dias inclusivos entre duas datas (ex: mesma data => 1 dia). */
private fun daysBetweenInclusive(start: Date, end: Date): Long {
    val days = ChronoUnit.DAYS.between(start.toInstant(), end.toInstant())
    return max(1, days + 1)
}
