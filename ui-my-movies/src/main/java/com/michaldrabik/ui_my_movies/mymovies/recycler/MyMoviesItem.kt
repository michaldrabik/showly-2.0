package com.michaldrabik.ui_my_movies.mymovies.recycler

import com.michaldrabik.ui_base.common.MovieListItem
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.MyMoviesSection
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.Translation
import java.time.format.DateTimeFormatter

data class MyMoviesItem(
  val type: Type,
  val header: Header?,
  val recentsSection: RecentsSection?,
  override val movie: Movie,
  override val image: Image,
  override val isLoading: Boolean,
  val spoilers: Spoilers,
  val translation: Translation? = null,
  val userRating: Int? = null,
  val dateFormat: DateTimeFormatter? = null,
  val sortOrder: SortOrder? = null
) : MovieListItem {

  enum class Type {
    HEADER,
    RECENT_MOVIES,
    ALL_MOVIES_ITEM
  }

  data class Header(
    val section: MyMoviesSection,
    val itemCount: Int,
    val sortOrder: Pair<SortOrder, SortType>?,
    val genres: List<Genre>?,
  )

  data class RecentsSection(
    val items: List<MyMoviesItem>,
  )

  data class Spoilers(
    val isSpoilerHidden: Boolean,
    val isSpoilerRatingsHidden: Boolean,
    val isSpoilerTapToReveal: Boolean,
  )

  companion object {

    fun createHeader(
      section: MyMoviesSection,
      itemCount: Int,
      sortOrder: Pair<SortOrder, SortType>?,
      genres: List<Genre>?,
    ) = MyMoviesItem(
      type = Type.HEADER,
      header = Header(section, itemCount, sortOrder, genres),
      recentsSection = null,
      movie = Movie.EMPTY,
      image = Image.createUnavailable(POSTER),
      isLoading = false,
      spoilers = Spoilers(
        isSpoilerHidden = false,
        isSpoilerRatingsHidden = false,
        isSpoilerTapToReveal = false
      )
    )

    fun createRecentsSection(
      movies: List<MyMoviesItem>,
    ) = MyMoviesItem(
      type = Type.RECENT_MOVIES,
      header = null,
      recentsSection = RecentsSection(movies),
      movie = Movie.EMPTY,
      image = Image.createUnavailable(POSTER),
      isLoading = false,
      spoilers = Spoilers(
        isSpoilerHidden = false,
        isSpoilerRatingsHidden = false,
        isSpoilerTapToReveal = false
      )
    )
  }
}
