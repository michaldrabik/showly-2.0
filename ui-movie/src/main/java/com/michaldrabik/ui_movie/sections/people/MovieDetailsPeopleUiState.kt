package com.michaldrabik.ui_movie.sections.people

import com.michaldrabik.ui_model.Person

data class MovieDetailsPeopleUiState(
  val isLoading: Boolean = true,
  val actors: List<Person>? = null,
  val crew: Map<Person.Department, List<Person>>? = null,
)
