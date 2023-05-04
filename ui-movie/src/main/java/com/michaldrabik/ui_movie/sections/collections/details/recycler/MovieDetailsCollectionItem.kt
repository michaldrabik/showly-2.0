package com.michaldrabik.ui_movie.sections.collections.details.recycler

import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Translation
import java.util.UUID

sealed class MovieDetailsCollectionItem {

  open val id: String
    get() = UUID.randomUUID().toString()

  data class HeaderItem(
    val title: String,
    val description: String,
  ) : MovieDetailsCollectionItem()

  data class MovieItem(
    val rank: Int,
    val movie: Movie,
    val image: Image,
    val isMyMovie: Boolean,
    val isWatchlist: Boolean,
    val translation: Translation?,
    val isLoading: Boolean,
  ) : MovieDetailsCollectionItem() {
    override val id get() = "${movie.traktId}"
  }

  object LoadingItem : MovieDetailsCollectionItem()
}
