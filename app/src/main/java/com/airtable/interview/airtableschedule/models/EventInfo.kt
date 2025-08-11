package com.airtable.interview.airtableschedule.models

import android.text.format.DateFormat

data class EventInfo(
    val event: Event,
    val offset: Int,
    val duration: Int,
    val startDate: String = DateFormat.format("MM-dd", event.startDate) as String,
    val endDate: String = DateFormat.format("MM-dd", event.endDate) as String,
    val dateScale: DateScale
)
