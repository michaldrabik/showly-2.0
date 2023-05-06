// ktlint-disable filename
package com.michaldrabik.ui_movie

import androidx.annotation.IdRes
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.MovieCollection
import com.michaldrabik.ui_model.Person

sealed class MovieDetailsEvent<T>(action: T) : Event<T>(action) {

  data class OpenPersonSheet(
    val movie: Movie,
    val person: Person
  ) : MovieDetailsEvent<Movie>(movie)

  data class OpenPeopleSheet(
    val movie: Movie,
    val people: List<Person>,
    val department: Person.Department
  ) : MovieDetailsEvent<Movie>(movie)

  data class OpenCollectionSheet(
    val movie: Movie,
    val collection: MovieCollection
  ) : MovieDetailsEvent<Movie>(movie)

  data class RemoveFromTrakt(
    @IdRes val navigationId: Int
  ) : MovieDetailsEvent<Int>(navigationId)

  data class SaveOpenedPerson(
    val person: Person
  ) : MovieDetailsEvent<Person>(person)

  data class SaveOpenedCollection(
    val collectionId: IdTrakt
  ) : MovieDetailsEvent<IdTrakt>(collectionId)

  object RequestWidgetsUpdate : MovieDetailsEvent<Unit>(Unit)

  object Finish : MovieDetailsEvent<Unit>(Unit)
}
