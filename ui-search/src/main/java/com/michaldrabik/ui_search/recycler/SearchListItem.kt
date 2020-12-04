package com.michaldrabik.ui_search.recycler

import com.michaldrabik.ui_base.common.ListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation

data class SearchListItem(
  override val show: Show,
  override val image: Image,
  override val isLoading: Boolean = false,
  val movie: Movie,
  val isFollowed: Boolean = false,
  val isWatchlist: Boolean = false,
  val translation: Translation? = null
) : ListItem {

  val isShow = show != Show.EMPTY
  val isMovie = movie != Movie.EMPTY

  val idTrakt = if (show != Show.EMPTY) show.traktId else movie.traktId
  val votes = if (show != Show.EMPTY) show.votes else movie.votes
  val title = if (show != Show.EMPTY) show.title else movie.title
  val overview = if (show != Show.EMPTY) show.overview else movie.overview
  val year = if (show != Show.EMPTY) show.year else movie.year
  val network = if (show != Show.EMPTY) show.network else ""

  override fun isSameAs(other: ListItem) =
    if (show != Show.EMPTY) show.traktId == other.show.traktId
    else movie.traktId == (other as SearchListItem).movie.traktId
}
