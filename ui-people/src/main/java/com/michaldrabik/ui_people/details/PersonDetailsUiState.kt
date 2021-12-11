package com.michaldrabik.ui_people.details

import com.michaldrabik.ui_people.details.recycler.PersonDetailsItem

data class PersonDetailsUiState(
  val personDetailsItems: List<PersonDetailsItem>? = null,
)
