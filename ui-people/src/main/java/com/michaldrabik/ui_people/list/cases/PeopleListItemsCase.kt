package com.michaldrabik.ui_people.list.cases

import com.michaldrabik.common.Mode
import com.michaldrabik.repository.PeopleRepository
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_people.list.recycler.PeopleListItem
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class PeopleListItemsCase @Inject constructor(
  private val peopleRepository: PeopleRepository,
) {

  suspend fun loadPeople(
    idTrakt: IdTrakt,
    mode: Mode,
    department: Person.Department
  ): List<PeopleListItem.PersonItem> {
    val ids = Ids.EMPTY.copy(trakt = idTrakt)
    val people: Map<Person.Department, List<Person>> = when (mode) {
      Mode.SHOWS -> peopleRepository.loadAllForShow(ids)
      Mode.MOVIES -> peopleRepository.loadAllForMovie(ids)
    }
    return people.getOrDefault(department, emptyList()).map {
      PeopleListItem.PersonItem(it)
    }
  }
}
