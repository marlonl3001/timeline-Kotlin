package com.airtable.interview.airtableschedule.presentation.timeline.state

import com.airtable.interview.airtableschedule.models.DateScale
import com.airtable.interview.airtableschedule.models.EventInfo
import com.airtable.interview.airtableschedule.utils.getDateScales

/**
 * UI state for the timeline screen.
 */
data class TimelineUiState(
    val eventInfos: List<EventInfo> = emptyList(),
    val dateScales: List<DateScale> = eventInfos.getDateScales()
)
