package com.michaldrabik.ui_my_shows.watchlist.recycler

import com.michaldrabik.common.extensions.toZonedDateTime
import com.michaldrabik.ui_base.common.ListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class WatchlistListItem(
  override val show: Show,
  override val image: Image,
  override val isLoading: Boolean = false,
  val dateFormat: DateTimeFormatter,
  val translation: Translation? = null,
  val userRating: Int? = null,
) : ListItem {

  fun getReleaseDate(): ZonedDateTime? = show.firstAired.toZonedDateTime()
}
