package com.michaldrabik.ui_progress_movies.progress.recycler

import androidx.annotation.StringRes
import com.michaldrabik.ui_base.common.MovieListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Translation
import java.time.format.DateTimeFormatter

sealed class ProgressMovieListItem(
  override val movie: Movie,
  override val image: Image,
  override val isLoading: Boolean = false,
) : MovieListItem {

  data class MovieItem(
    override val movie: Movie,
    override val image: Image,
    override val isLoading: Boolean = false,
    val isPinned: Boolean,
    val translation: Translation? = null,
    val dateFormat: DateTimeFormatter? = null,
  ) : ProgressMovieListItem(movie, image, isLoading)

  data class Header(
    override val movie: Movie,
    override val image: Image,
    override val isLoading: Boolean = false,
    @StringRes val textResId: Int,
  ) : ProgressMovieListItem(movie, image, isLoading) {

    companion object {
      fun create(@StringRes textResId: Int) =
        Header(
          movie = Movie.EMPTY,
          image = Image.createUnavailable(ImageType.POSTER),
          textResId = textResId
        )
    }

    override fun isSameAs(other: MovieListItem) =
      textResId == (other as? Header)?.textResId
  }
}
