package com.michaldrabik.showly2.ui.shows

import com.michaldrabik.showly2.model.Actor
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.ui.shows.related.RelatedListItem

data class ShowDetailsUiModel(
  val show: Show? = null,
  val showLoading: Boolean? = null,
  val image: Image? = null,
  val imageLoading: Boolean? = null,
  val nextEpisode: Episode? = null,
  val actors: List<Actor>? = null,
  val relatedShows: List<RelatedListItem>? = null,
  val updateRelatedShow: RelatedListItem? = null
)