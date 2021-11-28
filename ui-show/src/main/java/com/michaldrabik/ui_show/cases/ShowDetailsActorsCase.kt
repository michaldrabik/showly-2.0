package com.michaldrabik.ui_show.cases

import com.michaldrabik.repository.PeopleRepository
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import timber.log.Timber
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsActorsCase @Inject constructor(
  private val peopleRepository: PeopleRepository
) {

  suspend fun loadActors(show: Show): List<Person> {
    val people = peopleRepository.loadAllForShow(show.ids)
    return people.take(20)
  }

  suspend fun preloadDetails(people: List<Person>) = supervisorScope {
    val errorHandler = CoroutineExceptionHandler { _, _ -> Timber.d("Failed to preload details.") }
    people.take(5).forEach {
      launch(errorHandler) {
        peopleRepository.loadDetails(it)
      }
    }
  }
}
