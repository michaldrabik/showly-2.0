package com.michaldrabik.ui_my_movies.common.recycler

import com.michaldrabik.ui_base.common.MovieListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.Translation
import java.time.format.DateTimeFormatter

sealed class CollectionListItem(
  override val movie: Movie,
  override val image: Image,
  override val isLoading: Boolean = false,
) : MovieListItem {

  data class MovieItem(
    override val movie: Movie,
    override val image: Image,
    override val isLoading: Boolean = false,
    val dateFormat: DateTimeFormatter,
    val fullDateFormat: DateTimeFormatter,
    val translation: Translation? = null,
    val userRating: Int? = null,
    val sortOrder: SortOrder? = null,
  ) : CollectionListItem(
    movie = movie,
    image = image,
    isLoading = isLoading
  )

  data class FiltersItem(
    val sortOrder: SortOrder,
    val sortType: SortType,
    val isUpcoming: Boolean,
  ) : CollectionListItem(
    movie = Movie.EMPTY,
    image = Image.createUnknown(ImageType.FILTERS),
    isLoading = false
  )
}
