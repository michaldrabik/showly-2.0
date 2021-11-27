package com.michaldrabik.ui_show.cases

import com.michaldrabik.repository.PeopleRepository
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsActorsCase @Inject constructor(
  private val peopleRepository: PeopleRepository
) {

  suspend fun loadActors(show: Show): List<Person> {
    val people = peopleRepository.loadAllForShow(show.ids)
    return people.take(20)
  }
}
