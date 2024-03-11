package com.michaldrabik.ui_progress.history.utilities.groupers

import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.ui_progress.history.entities.HistoryListItem
import java.time.temporal.ChronoUnit.DAYS
import javax.inject.Inject

internal class HistoryItemsGrouper @Inject constructor() {

  fun groupByDay(
    items: List<HistoryListItem.Episode>,
    language: String
  ): List<HistoryListItem> {
    val itemsMap = items
      .groupBy { it.episode.lastWatchedAt!!.toLocalZone().truncatedTo(DAYS) }
      .toSortedMap(compareByDescending { it.toMillis() })

    return itemsMap.entries.fold(mutableListOf()) { acc, entry ->
      acc.apply {
        if (entry.value.isNotEmpty()) {
          add(
            HistoryListItem.Header(
              date = entry.key.toLocalDateTime(),
              language = language
            )
          )
          addAll(entry.value.sortedByDescending { it.episode.lastWatchedAt?.toMillis() })
        }
      }
    }
  }
}
