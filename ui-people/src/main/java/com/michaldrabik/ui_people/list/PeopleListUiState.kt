package com.michaldrabik.ui_people.list

import com.michaldrabik.ui_people.list.recycler.PeopleListItem

data class PeopleListUiState(
  val peopleItems: List<PeopleListItem>? = null,
)
