package com.michaldrabik.ui_movie.cases

import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Translation
import javax.inject.Inject

@AppScope
class MovieDetailsTranslationCase @Inject constructor(
  private val translationsRepository: TranslationsRepository
) {

  suspend fun loadTranslation(movie: Movie): Translation? {
    val language = translationsRepository.getLanguage()
    if (language == DEFAULT_LANGUAGE) return null
    return translationsRepository.loadTranslation(movie, language)
  }
}
