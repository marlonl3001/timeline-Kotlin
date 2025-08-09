package com.airtable.interview.airtableschedule.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.airtable.interview.airtableschedule.models.Event
import com.airtable.interview.airtableschedule.utils.calculateEventPosition
import java.time.temporal.ChronoUnit
import java.util.Date

/**
 * Desenha as lanes de eventos proporcionalmente à duração.
 * @param lanes Lista de lanes, cada lane é uma lista de eventos.
 * @param minDate Data mínima do período.
 * @param maxDate Data máxima do período.
 */
@Composable
fun TimelineLanes(
    lanes: List<List<Event>>,
    minDate: Date,
    maxDate: Date
) {
    val totalDays = ChronoUnit.DAYS.between(minDate.toInstant(), maxDate.toInstant()) + 1

    Column(modifier = Modifier.fillMaxWidth()) {
        lanes.forEachIndexed { index, lane ->
            Row(Modifier.fillMaxWidth()) {
                lane.forEach { event ->
                    val (startOffset, duration) = calculateEventPosition(event, minDate)

                    if (startOffset > 0f) {
                        // Espaço vazio antes do evento
                        Spacer(Modifier.weight(startOffset.toFloat()))
                    }

                    // Caixa do evento
                    Box(
                        Modifier
                            .weight(duration.toFloat())
                            .height(48.dp)
                            .background(
                                color = Color(0xFFBB86FC), // Cor base (pode ser variada)
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(4.dp)
                    ) {
                        Text(
                            text = event.name,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
