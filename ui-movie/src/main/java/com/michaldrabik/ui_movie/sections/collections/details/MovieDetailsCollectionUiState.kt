package com.michaldrabik.ui_movie.sections.collections.details

import com.michaldrabik.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem

data class MovieDetailsCollectionUiState(
  val items: List<MovieDetailsCollectionItem>? = null,
)
