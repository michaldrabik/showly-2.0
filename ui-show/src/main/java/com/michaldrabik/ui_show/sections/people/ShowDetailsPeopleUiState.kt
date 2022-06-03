package com.michaldrabik.ui_show.sections.people

import com.michaldrabik.ui_model.Person

data class ShowDetailsPeopleUiState(
  val isLoading: Boolean = true,
  val actors: List<Person>? = null,
  val crew: Map<Person.Department, List<Person>>? = null,
)
