package com.michaldrabik.ui_movie.cases

import com.michaldrabik.repository.PeopleRepository
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Person
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsActorsCase @Inject constructor(
  private val peopleRepository: PeopleRepository
) {

  suspend fun loadActors(movie: Movie): List<Person> {
    val people = peopleRepository.loadAllForMovie(movie.ids)
    return people.take(20)
  }
}
