package com.michaldrabik.ui_movie.cases

import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TranslationsRepository
import javax.inject.Inject

@AppScope
class MovieDetailsTranslationCase @Inject constructor(
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository
) {

  suspend fun loadTranslation(movie: Movie): Translation? {
    val language = settingsRepository.getLanguage()
    if (language == DEFAULT_LANGUAGE) return null
    return translationsRepository.loadTranslation(movie, language)
  }
}
