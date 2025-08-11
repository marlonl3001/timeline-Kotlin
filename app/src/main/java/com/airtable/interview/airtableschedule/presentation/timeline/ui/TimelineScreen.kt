package com.airtable.interview.airtableschedule.presentation.timeline.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airtable.interview.airtableschedule.models.DateScale
import com.airtable.interview.airtableschedule.models.EventInfo
import com.airtable.interview.airtableschedule.presentation.timeline.viewmodel.TimelineViewModel

/**
 * TimelineScreen: composable entry shown from MainActivity.
 * Uses the existing ViewModel to obtain events and then renders swimlanes.
 */
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TimelineView(
        eventInfos = uiState.eventInfos.sortedBy { event -> event.startDate },
        dateScales = uiState.dateScales
    )
}

/**
 * Top-level view that renders lanes with a date scale and swimlane rows
 */
@Composable
private fun TimelineView(eventInfos: List<EventInfo>, dateScales: List<DateScale>) {
    if (eventInfos.isEmpty()) {
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
        ) {
            Column {
                DateScale(dateScales = dateScales)
                Spacer(modifier = Modifier.height(8.dp))
                SwimlaneRow(eventInfos, dateScales)
            }
        }
    }
}

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
 * Timeline lane with events arranged proportionally.
 * Ensures correct spacing and avoids zero weight in the Spacer.
 */
@Composable
private fun SwimlaneRow(eventInfos: List<EventInfo>, dateScales: List<DateScale>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            eventInfos.forEach { info ->
                val startIndex = dateScales.indexOf(dateScales.find { "${it.monthNumber}-${it.day}" == "${info.dateScale.monthNumber}-${info.dateScale.day}"})
                val endIndex = dateScales.indexOf(dateScales.find { "${it.monthNumber}-${it.day}" == info.endDate})

                val eventDuration = (endIndex - startIndex) + 1
                Row {
                    if (startIndex > 0) {
                        Spacer(modifier = Modifier.width((startIndex * 80).dp))
                    }
                    EventBox(modifier = Modifier.width((eventDuration * 80).dp), eventInfo = info)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Individual box for an event, with width proportional to the duration.
 * Fixed colors to vary visually between events.
 */
@Composable
private fun EventBox(modifier: Modifier = Modifier, eventInfo: EventInfo) {
    val colors = listOf(
        0xFF7C4DFF, // purple
        0xFF03DAC5, // teal
        0xFFFFB74D, // orange
        0xFF90CAF9, // light blue
        0xFFEF9A9A  // light red
    )
    val color = Color(colors[eventInfo.event.id % colors.size])

    Card(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Minimum width to avoid very small boxes
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
