package com.michaldrabik.showly2.ui.statistics.views.ratings.recycler

import com.michaldrabik.showly2.ui.discover.recycler.ListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.TraktRating

data class StatisticsRatingItem(
  override val show: Show,
  override val image: Image,
  override val isLoading: Boolean,
  val rating: TraktRating
) : ListItem
