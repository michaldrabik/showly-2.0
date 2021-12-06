package com.michaldrabik.ui_movie.cases

import com.michaldrabik.repository.PeopleRepository
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Person
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import timber.log.Timber
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsActorsCase @Inject constructor(
  private val peopleRepository: PeopleRepository
) {

  suspend fun loadActors(movie: Movie): List<Person> {
    val people = peopleRepository.loadAllForMovie(movie.ids)
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
