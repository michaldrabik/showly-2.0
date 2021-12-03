package com.michaldrabik.ui_people.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.Mode
import com.michaldrabik.repository.PeopleRepository
import com.michaldrabik.repository.TranslationsRepository
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
  private val translationsRepository: TranslationsRepository,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider
) {

  suspend fun loadCredits(
    person: Person,
    filters: List<Mode>,
    language: String
  ): Map<Int?, List<PersonCredit>> {
    val result = peopleRepository.loadCredits(person)
    return result
      .filter {
        when {
          filters.isEmpty() || filters.containsAll(Mode.values().toList()) -> true
          filters.contains(Mode.SHOWS) -> it.show != null
          filters.contains(Mode.MOVIES) -> it.movie != null
          else -> true
        }
      }
      .filter { it.releaseDate != null || (it.releaseDate == null && it.isUpcoming) }
      .map {
        it.show?.let { show ->
          val image = showImagesProvider.findCachedImage(show, ImageType.POSTER)
          val translation = when (language) {
            Config.DEFAULT_LANGUAGE -> null
            else -> translationsRepository.loadTranslation(show, language, onlyLocal = true)
          }
          return@map it.copy(image = image, translation = translation)
        }
        it.movie?.let { movie ->
          val image = movieImagesProvider.findCachedImage(movie, ImageType.POSTER)
          val translation = when (language) {
            Config.DEFAULT_LANGUAGE -> null
            else -> translationsRepository.loadTranslation(movie, language, onlyLocal = true)
          }
          return@map it.copy(image = image, translation = translation)
        }
        throw IllegalStateException()
      }
      .sortedWith(
        compareByDescending<PersonCredit> { it.releaseDate == null }.thenByDescending { it.releaseDate?.toEpochDay() }
      )
      .groupBy { it.releaseDate?.year }
  }
}
