package com.michaldrabik.ui_people.cases

import com.michaldrabik.repository.PeopleRepository
import com.michaldrabik.ui_model.Person
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class PersonDetailsLoadCase @Inject constructor(
  private val peopleRepository: PeopleRepository
) {

  suspend fun loadDetails(person: Person): Person {
    val personDetails = peopleRepository.loadDetails(person)
    return personDetails.copy(character = person.character)
  }
}
