package com.michaldrabik.ui_progress.helpers

import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressItemsSorter @Inject constructor() {
  fun sort(sortOrder: SortOrder) =
    when (sortOrder) {
      SortOrder.NAME -> compareBy { getTitle(it) }
      SortOrder.RECENTLY_WATCHED -> compareByDescending { it.show.updatedAt }
      SortOrder.NEWEST -> compareByDescending { it.episode?.firstAired?.toMillis() }
      SortOrder.EPISODES_LEFT -> compareBy<ProgressListItem.Episode> { it.totalCount - it.watchedCount }
        .thenBy { getTitle(it) }
      else -> throw IllegalStateException("Invalid sort order")
    }

  private fun getTitle(item: ProgressListItem.Episode): String {
    val translatedTitle =
      if (item.translations?.show?.hasTitle == false) null
      else item.translations?.show?.title
    return (translatedTitle ?: item.show.titleNoThe).uppercase(Locale.ROOT)
  }
}
