package com.michaldrabik.showly2.ui.statistics.views.mostWatched

import com.michaldrabik.showly2.ui.discover.recycler.ListItem
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Show

data class StatisticsMostWatchedItem(
  override val show: Show,
  val episodes: List<Episode>,
  val seasonsCount: Long,
  override val image: Image,
  override val isLoading: Boolean = false,
  val isArchived: Boolean
) : ListItem
