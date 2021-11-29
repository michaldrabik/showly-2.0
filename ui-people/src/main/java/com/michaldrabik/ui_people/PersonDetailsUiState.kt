package com.michaldrabik.ui_people

import com.michaldrabik.ui_people.recycler.PersonDetailsItem

data class PersonDetailsUiState(
  val personDetailsItems: List<PersonDetailsItem>? = null,
)
