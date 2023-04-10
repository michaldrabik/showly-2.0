package com.michaldrabik.ui_show.sections.people.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.PeopleRepository
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsPeopleCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val peopleRepository: PeopleRepository
) {

  suspend fun loadPeople(show: Show) = withContext(dispatchers.IO) {
    peopleRepository.loadAllForShow(show.ids)
  }

  suspend fun preloadDetails(people: List<Person>) =
    supervisorScope {
      val errorHandler = CoroutineExceptionHandler { _, _ -> Timber.d("Failed to preload details.") }
      people.take(5).forEach {
        launch(errorHandler) {
          withContext(dispatchers.IO) {
            peopleRepository.loadDetails(it)
          }
        }
      }
    }
}
