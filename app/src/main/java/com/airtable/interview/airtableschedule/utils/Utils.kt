package com.airtable.interview.airtableschedule.utils

import android.text.format.DateFormat
import com.airtable.interview.airtableschedule.models.DateScale
import com.airtable.interview.airtableschedule.models.EventInfo
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import kotlin.math.max

/** Dias inclusivos entre duas datas (ex: mesma data => 1 dia). */
fun daysBetweenInclusive(start: Date, end: Date): Long {
    val days = ChronoUnit.DAYS.between(start.toInstant(), end.toInstant())
    return max(1, days + 1)
}

fun List<EventInfo>.getDateScales(): List<DateScale> {
    val dateScales = mutableListOf<DateScale>()
    val map = mutableMapOf<String, String>()

    this.forEach { eventInfo ->
        if (!map.containsKey(eventInfo.startDate)) {
            map[eventInfo.startDate] = eventInfo.startDate
            dateScales.add(eventInfo.dateScale.copy(monthNumber = eventInfo.event.startDate.month.getMonthNumber()))
        }
        if (!map.containsKey(eventInfo.endDate)) {
            map[eventInfo.endDate] = eventInfo.endDate
            dateScales.add(
                DateScale(
                    dayOfTheWeek = DateFormat.format("EEE", eventInfo.event.endDate) as String,
                    day = DateFormat.format("dd", eventInfo.event.endDate) as String,
                    monthName = DateFormat.format("MMM", eventInfo.event.endDate) as String,
                    monthNumber = eventInfo.event.endDate.month.getMonthNumber()
                )
            )
        }
    }

    return dateScales.sortedWith(compareBy({it.monthName}, {it.day}))
}

private fun Int.getMonthNumber(): String =
    String.format(Locale.getDefault(), "%02d", this + 1)
