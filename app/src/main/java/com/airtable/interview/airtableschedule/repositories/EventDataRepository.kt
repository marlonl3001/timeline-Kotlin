package com.airtable.interview.airtableschedule.repositories

import android.text.format.DateFormat
import com.airtable.interview.airtableschedule.models.DateScale
import com.airtable.interview.airtableschedule.models.Event
import com.airtable.interview.airtableschedule.models.EventInfo
import com.airtable.interview.airtableschedule.models.SampleTimelineItems
import com.airtable.interview.airtableschedule.utils.daysBetweenInclusive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Locale

/**
 * A store for data related to events. Currently, this just returns sample data.
 */
interface EventDataRepository {
    fun getTimelineItems(): Flow<List<EventInfo>>
}

class EventDataRepositoryImpl : EventDataRepository {
    override fun getTimelineItems(): Flow<List<EventInfo>> {
        return flowOf(SampleTimelineItems.timelineItems.toModel())
    }
}

fun List<Event>.toModel(): List<EventInfo> {
    // Compute global bounds
    val minDate = this.minOf { it.startDate }
    val eventInfos = this.map { event ->
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
    return eventInfos
}
