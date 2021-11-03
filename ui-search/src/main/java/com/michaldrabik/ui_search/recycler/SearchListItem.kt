package com.michaldrabik.ui_search.recycler

import com.michaldrabik.ui_base.common.ListItem
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import java.util.UUID

data class SearchListItem(
  val id: UUID,
  override val show: Show,
  override val image: Image,
  override val isLoading: Boolean = false,
  val movie: Movie,
  val score: Float,
  val isFollowed: Boolean = false,
  val isWatchlist: Boolean = false,
  val translation: Translation? = null
) : ListItem {

  val isShow = show != Show.EMPTY
  val isMovie = movie != Movie.EMPTY

  val votes = if (isShow) show.votes else movie.votes
  val title = if (isShow) show.title else movie.title
  val overview = if (isShow) show.overview else movie.overview
  val year = if (isShow) show.year else movie.year
  val network = if (isShow) show.network else ""

  override fun isSameAs(other: ListItem) = (id == (other as SearchListItem).id)
}
