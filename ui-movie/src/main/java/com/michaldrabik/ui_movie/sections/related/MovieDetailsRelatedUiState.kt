package com.michaldrabik.ui_movie.sections.related

import com.michaldrabik.ui_movie.sections.related.recycler.RelatedListItem

data class MovieDetailsRelatedUiState(
  val isLoading: Boolean = true,
  val relatedMovies: List<RelatedListItem>? = null,
)
