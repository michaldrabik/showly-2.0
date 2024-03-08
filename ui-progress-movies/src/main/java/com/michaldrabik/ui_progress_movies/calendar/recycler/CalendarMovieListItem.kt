package com.michaldrabik.ui_progress_movies.calendar.recycler

import androidx.annotation.StringRes
import com.michaldrabik.ui_base.common.MovieListItem
import com.michaldrabik.ui_model.CalendarMode
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SpoilersSettings
import com.michaldrabik.ui_model.Translation
import java.time.format.DateTimeFormatter

sealed class CalendarMovieListItem(
  override val movie: Movie,
  override val image: Image,
  override val isLoading: Boolean = false,
) : MovieListItem {

  data class MovieItem(
    override val movie: Movie,
    override val image: Image,
    override val isLoading: Boolean = false,
    val isWatched: Boolean,
    val isWatchlist: Boolean,
    val translation: Translation? = null,
    val dateFormat: DateTimeFormatter? = null,
    val spoilers: SpoilersSettings
  ) : CalendarMovieListItem(movie, image, isLoading)

  data class Header(
    @StringRes val textResId: Int,
    val calendarMode: CalendarMode,
  ) : CalendarMovieListItem(
    movie = Movie.EMPTY,
    image = Image.createUnknown(ImageType.POSTER),
    isLoading = false
  ) {

    companion object {
      fun create(@StringRes textResId: Int, mode: CalendarMode) =
        Header(
          textResId = textResId,
          calendarMode = mode
        )
    }

    override fun isSameAs(other: MovieListItem) =
      textResId == (other as? Header)?.textResId
  }

  data class Filters(
    val mode: CalendarMode,
  ) : CalendarMovieListItem(
    movie = Movie.EMPTY,
    image = Image.createUnknown(ImageType.POSTER),
    isLoading = false
  )
}
