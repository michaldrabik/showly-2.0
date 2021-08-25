package com.michaldrabik.ui_my_movies.mymovies.recycler

import com.michaldrabik.ui_base.common.MovieListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.MyMoviesSection
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.Translation
import java.time.format.DateTimeFormatter

data class MyMoviesItem(
  val type: Type,
  val header: Header?,
  val recentsSection: RecentsSection?,
  val horizontalSection: HorizontalSection?,
  override val movie: Movie,
  override val image: Image,
  override val isLoading: Boolean,
  val translation: Translation? = null,
  val userRating: Int? = null,
  val dateFormat: DateTimeFormatter? = null
) : MovieListItem {

  enum class Type {
    HEADER,
    RECENT_MOVIE,
    ALL_MOVIES_ITEM,
    SEARCH_MOVIES_ITEM
  }

  data class Header(
    val section: MyMoviesSection,
    val itemCount: Int,
    val sortOrder: SortOrder?
  )

  data class RecentsSection(
    val items: List<MyMoviesItem>
  )

  data class HorizontalSection(
    val section: MyMoviesSection,
    val items: List<MyMoviesItem>
  )

  companion object {
    fun createHeader(
      section: MyMoviesSection,
      itemCount: Int,
      sortOrder: SortOrder?
    ) = MyMoviesItem(
      Type.HEADER,
      Header(section, itemCount, sortOrder),
      null,
      null,
      Movie.EMPTY,
      Image.createUnavailable(POSTER),
      false
    )

    fun createRecentsSection(
      movies: List<MyMoviesItem>
    ) = MyMoviesItem(
      Type.RECENT_MOVIE,
      null,
      RecentsSection(movies),
      null,
      Movie.EMPTY,
      Image.createUnavailable(POSTER),
      false
    )

    fun createSearchItem(
      movie: Movie,
      image: Image,
      translation: Translation? = null
    ) = MyMoviesItem(
      Type.SEARCH_MOVIES_ITEM,
      null,
      null,
      null,
      movie,
      image,
      false,
      translation
    )
  }
}
