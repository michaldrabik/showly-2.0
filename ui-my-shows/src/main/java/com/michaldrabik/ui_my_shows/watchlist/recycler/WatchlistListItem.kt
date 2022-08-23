package com.michaldrabik.ui_my_shows.watchlist.recycler

import com.michaldrabik.common.extensions.toZonedDateTime
import com.michaldrabik.ui_base.common.ListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.Translation
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

sealed class WatchlistListItem(
  override val show: Show,
  override val image: Image,
  override val isLoading: Boolean = false,
) : ListItem {

  fun getReleaseDate(): ZonedDateTime? = show.firstAired.toZonedDateTime()

  data class ShowItem(
    override val show: Show,
    override val image: Image,
    override val isLoading: Boolean = false,
    val dateFormat: DateTimeFormatter,
    val translation: Translation? = null,
    val userRating: Int? = null,
  ) : WatchlistListItem(
    show = show,
    image = image,
    isLoading = isLoading
  )

  data class FiltersItem(
    val sortOrder: SortOrder,
    val sortType: SortType,
  ) : WatchlistListItem(
    show = Show.EMPTY,
    image = Image.createUnknown(ImageType.POSTER),
    isLoading = false
  )
}
