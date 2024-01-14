package com.michaldrabik.ui_statistics.views.mostWatched

import com.michaldrabik.ui_base.common.ListItem
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation

data class StatisticsMostWatchedItem(
  override val show: Show,
  val episodes: List<Episode>,
  val seasonsCount: Long,
  override val image: Image,
  override val isLoading: Boolean = false,
  val translation: Translation? = null
) : ListItem
