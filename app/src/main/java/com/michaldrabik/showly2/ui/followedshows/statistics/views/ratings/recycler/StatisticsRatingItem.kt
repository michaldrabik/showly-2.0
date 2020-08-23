package com.michaldrabik.showly2.ui.followedshows.statistics.views.ratings.recycler

import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.TraktRating
import com.michaldrabik.showly2.ui.discover.recycler.ListItem

data class StatisticsRatingItem(
  override val show: Show,
  override val image: Image,
  override val isLoading: Boolean,
  val rating: TraktRating
) : ListItem
