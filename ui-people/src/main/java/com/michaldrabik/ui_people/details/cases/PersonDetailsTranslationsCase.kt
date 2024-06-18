package com.michaldrabik.ui_people.details.cases

import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_model.locale.AppLocale
import com.michaldrabik.ui_people.details.recycler.PersonDetailsItem
import dagger.hilt.android.scopes.ViewModelScoped
import timber.log.Timber
import javax.inject.Inject

@ViewModelScoped
class PersonDetailsTranslationsCase @Inject constructor(
  private val translationsRepository: TranslationsRepository,
) {

  suspend fun loadMissingTranslation(item: PersonDetailsItem.CreditsShowItem, locale: AppLocale) =
    try {
      val translation = translationsRepository.loadTranslation(item.show, locale) ?: Translation.EMPTY
      item.copy(isLoading = false, translation = translation)
    } catch (error: Throwable) {
      Timber.w(error)
      item.copy(isLoading = false, translation = Translation.EMPTY)
    }

  suspend fun loadMissingTranslation(item: PersonDetailsItem.CreditsMovieItem, locale: AppLocale) =
    try {
      val translation = translationsRepository.loadTranslation(item.movie, locale) ?: Translation.EMPTY
      item.copy(isLoading = false, translation = translation)
    } catch (error: Throwable) {
      Timber.w(error)
      item.copy(isLoading = false, translation = Translation.EMPTY)
    }
}
