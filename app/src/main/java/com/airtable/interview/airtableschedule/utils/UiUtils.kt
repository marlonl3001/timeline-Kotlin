package com.airtable.interview.airtableschedule.utils

import com.airtable.interview.airtableschedule.models.Event
import java.time.temporal.ChronoUnit
import java.util.Date

/**
 * Takes a list of [Event]s and assigns them to lanes based on start/end dates.
 */
fun assignLanes(events: List<Event>): List<List<Event>> {
    val lanes = mutableListOf<MutableList<Event>>()

    // Go through the list of events sorted by start date
    events.sortedBy { event -> event.startDate }
        .forEach { event ->
            // Attempt to assign the event to an existing lane
            val availableLane = lanes.find { lane ->
                lane.last().endDate < event.startDate
            }

            if (availableLane != null) {
                availableLane.add(event)
            } else {
                // Create a new lane if there are currently no free lanes to assign the event
                lanes.add(mutableListOf(event))
            }
        }
    return lanes
}

/**
 * Retorna o par (minDate, maxDate) do conjunto de eventos.
 */
fun findTimelineBounds(events: List<Event>): Pair<Date, Date> {
    val minDate = events.minOf { it.startDate }
    val maxDate = events.maxOf { it.endDate }
    return minDate to maxDate
}

/**
 * Calcula o deslocamento inicial e a duração de um evento, em dias,
 * baseado em um intervalo mínimo/máximo.
 */
fun calculateEventPosition(
    event: Event,
    minDate: Date
): Pair<Long, Long> {
    val startOffset = ChronoUnit.DAYS.between(minDate.toInstant(), event.startDate.toInstant())
    val duration = ChronoUnit.DAYS.between(event.startDate.toInstant(), event.endDate.toInstant()) + 1
    return startOffset to duration
}
