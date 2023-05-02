package com.michaldrabik.ui_movie.sections.collections

import com.michaldrabik.repository.movies.MovieCollectionsRepository.Source
import com.michaldrabik.ui_model.MovieCollection

data class MovieDetailsCollectionUiState(
  val isLoading: Boolean = true,
  val collections: Pair<List<MovieCollection>, Source>? = null,
)
