package com.michaldrabik.ui_show.cases

import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_repository.TranslationsRepository
import java.util.*
import javax.inject.Inject

@AppScope
class ShowDetailsTranslationCase @Inject constructor(
  private val translationsRepository: TranslationsRepository
) {

  suspend fun loadTranslation(show: Show): Translation? {
    val locale = Locale.getDefault()
    if (locale.language == DEFAULT_LANGUAGE) return null
    return translationsRepository.loadTranslation(show, locale)
  }

  suspend fun loadTranslation(episode: Episode, show: Show, onlyLocal: Boolean = false): Translation? {
    val locale = Locale.getDefault()
    if (locale.language == DEFAULT_LANGUAGE) return null
    return translationsRepository.loadTranslation(episode, show.ids.trakt, locale, onlyLocal)
  }
}
