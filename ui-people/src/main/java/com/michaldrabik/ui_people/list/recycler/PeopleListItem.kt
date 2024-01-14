package com.michaldrabik.ui_people.list.recycler

import com.michaldrabik.ui_model.Person

sealed class PeopleListItem {

  data class PersonItem(
    val person: Person,
  ) : PeopleListItem()

  data class HeaderItem(
    val department: Person.Department,
    val mediaTitle: String
  ) : PeopleListItem()
}
