package com.michaldrabik.ui_progress_movies

import com.michaldrabik.ui_base.common.MovieListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Translation
import org.threeten.bp.format.DateTimeFormatter

data class ProgressMovieItem(
  override val movie: Movie,
  override val image: Image,
  override val isLoading: Boolean = false,
  val headerTextResId: Int? = null,
  val isPinned: Boolean = false,
  val movieTranslation: Translation? = null,
  val dateFormat: DateTimeFormatter? = null
) : MovieListItem {

  fun isSameAs(other: ProgressMovieItem) =
    movie.ids.trakt == other.movie.ids.trakt && isHeader() == other.isHeader()

  fun isHeader() = headerTextResId != null

  companion object {
    val EMPTY = ProgressMovieItem(
      Movie.EMPTY,
      Image.createUnavailable(ImageType.POSTER)
    )
  }
}
