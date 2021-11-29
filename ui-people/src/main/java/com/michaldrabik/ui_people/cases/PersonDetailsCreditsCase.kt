package com.michaldrabik.ui_people.cases

import com.michaldrabik.repository.PeopleRepository
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.PersonCredit
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class PersonDetailsCreditsCase @Inject constructor(
  private val peopleRepository: PeopleRepository,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider
) {

  suspend fun loadCredits(person: Person): List<PersonCredit> {
    val result = peopleRepository.loadCredits(person)
    return result
      .map {
        it.show?.let { show -> return@map it.copy(image = showImagesProvider.findCachedImage(show, ImageType.POSTER)) }
        it.movie?.let { movie -> return@map it.copy(image = movieImagesProvider.findCachedImage(movie, ImageType.POSTER)) }
        throw IllegalStateException()
      }
      .sortedWith(compareBy<PersonCredit> { (it.year ?: 0) > 0 }.thenByDescending { it.year })
  }
}
