package com.airtable.interview.airtableschedule.presentation.timeline

import android.text.format.DateFormat
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airtable.interview.airtableschedule.models.Event
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
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

    TimelineView(events = uiState.events.sortedBy { event -> event.startDate })
}

/**
 * Top-level view that:
 * 1) Guards empty state
 * 2) Computes global min/max date and totalDays
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val eventInfos = events.map { event ->
            val offset = daysBetweenInclusive(minDate, event.startDate).toInt() - 1 // zero-based offset
            val duration = daysBetweenInclusive(event.startDate, event.endDate).toInt()
            EventInfo(
                event = event,
                offset = offset.coerceAtLeast(0),
                duration = duration.coerceAtLeast(1),
                dateScale = DateScale(
                    dayOfTheWeek = DateFormat.format("EEE", event.startDate) as String,
                    day = DateFormat.format("dd", event.startDate) as String,
                    monthName = DateFormat.format("MMM", event.startDate) as String,
                    monthNumber = String.format(Locale.getDefault(), "%02d", event.startDate.month + 1)
                )
            )
        }
        val dateScales = eventInfos.getDateScales()

        // Render a simple date scale (labels a cada ~6 divisões para evitar poluição)
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
        ) {
            Column {
                DateScale(dateScales = dateScales)

                Spacer(modifier = Modifier.height(8.dp))

                // Render each lane. Cada lane é scrollável horizontalmente para suportar timelines longas.
                SwimlaneRow(eventInfos, dateScales)
            }
        }
    }
}

/**
 * Uma escala horizontal simples de datas, exibindo um rótulo a cada N dias para legibilidade.
 */
@Composable
private fun DateScale(dateScales: List<DateScale>) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dateScales.forEach { dateScale ->
            Card(
                modifier = Modifier
                    .width(80.dp)
                    .padding(4.dp)
                    .animateContentSize(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dateScale.monthName.uppercase(),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = dateScale.dayOfTheWeek.uppercase(),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateScale.day.uppercase()
                    )
                }
            }
        }
    }
}

/**
 * Linha (lane) da timeline com eventos dispostos proporcionalmente.
 * Garante espaçamento correto e evita peso zero no Spacer.
 */
@Composable
private fun SwimlaneRow(eventInfos: List<EventInfo>, dateScales: List<DateScale>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            eventInfos.forEach { info ->
                val startIndex = dateScales.indexOf(dateScales.find { "${it.monthNumber}-${it.day}" == "${info.dateScale.monthNumber}-${info.dateScale.day}" })
                val endIndex = dateScales.indexOf(dateScales.find { "${it.monthNumber}-${it.day}" == info.endDate })

                val eventDuration = (endIndex - startIndex) + 1
                Row {
                    if (info.offset > 0) {
                        Spacer(modifier = Modifier.width((info.offset * 80).dp))
                    }
                    EventBox(modifier = Modifier.width((eventDuration * 80).dp), eventInfo = info)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Caixa individual para um evento, com largura proporcional à duração.
 * Cores fixas para variar visualmente entre eventos.
 */
@Composable
private fun EventBox(modifier: Modifier = Modifier, eventInfo: EventInfo) {
    val colors = listOf(
        0xFF7C4DFF, // roxo
        0xFF03DAC5, // teal
        0xFFFFB74D, // laranja
        0xFF90CAF9, // azul claro
        0xFFEF9A9A  // vermelho claro
    )
    val color = androidx.compose.ui.graphics.Color(colors[eventInfo.event.id % colors.size])

    Card(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Largura mínima para evitar caixas muito pequenas
                .widthIn(min = 80.dp)
                .background(color)
                .padding(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = eventInfo.event.name,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                softWrap = true,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${eventInfo.startDate} - ${eventInfo.endDate}",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

private data class EventInfo(
    val event: Event,
    val offset: Int,
    val duration: Int,
    val startDate: String = DateFormat.format("MM-dd", event.startDate) as String,
    val endDate: String = DateFormat.format("MM-dd", event.endDate) as String,
    val dateScale: DateScale
)

private data class DateScale(
    val dayOfTheWeek: String,
    val day: String,
    val monthName: String,
    val monthNumber: String
)

/** Dias inclusivos entre duas datas (ex: mesma data => 1 dia). */
private fun daysBetweenInclusive(start: Date, end: Date): Long {
    val days = ChronoUnit.DAYS.between(start.toInstant(), end.toInstant())
    return max(1, days + 1)
}

private fun List<EventInfo>.getDateScales(): List<DateScale> {
    val dateScales = mutableListOf<DateScale>()
    val map = mutableMapOf<String, String>()

    this.forEach { eventInfo ->
        val monthNumber = String.format(Locale.getDefault(), "%02d", eventInfo.event.endDate.month + 1)
        if (!map.containsKey(eventInfo.startDate)) {
            map[eventInfo.startDate] = eventInfo.startDate
            dateScales.add(eventInfo.dateScale.copy(monthNumber = monthNumber))
        }
        if (!map.containsKey(eventInfo.endDate)) {
            map[eventInfo.endDate] = eventInfo.endDate
            dateScales.add(
                DateScale(
                    dayOfTheWeek = DateFormat.format("EEE", eventInfo.event.endDate) as String,
                    day = DateFormat.format("dd", eventInfo.event.endDate) as String,
                    monthName = DateFormat.format("MMM", eventInfo.event.endDate) as String,
                    monthNumber = monthNumber
                )
            )
        }
    }

    return dateScales.sortedWith(compareBy({it.monthName}, {it.day}))
}
