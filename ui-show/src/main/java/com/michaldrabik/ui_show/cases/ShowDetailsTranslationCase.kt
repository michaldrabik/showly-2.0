package com.michaldrabik.ui_show.cases

import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.SeasonTranslation
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsTranslationCase @Inject constructor(
  private val translationsRepository: TranslationsRepository
) {

  suspend fun loadTranslation(show: Show): Translation? {
    val language = translationsRepository.getLanguage()
    if (language == DEFAULT_LANGUAGE) return null
    return translationsRepository.loadTranslation(show, language)
  }

  suspend fun loadTranslation(episode: Episode, show: Show, onlyLocal: Boolean = false): Translation? {
    val language = translationsRepository.getLanguage()
    if (language == DEFAULT_LANGUAGE) return null
    return translationsRepository.loadTranslation(episode, show.ids.trakt, language, onlyLocal)
  }

  suspend fun loadTranslations(season: Season, show: Show, onlyLocal: Boolean = false): List<SeasonTranslation> {
    val language = translationsRepository.getLanguage()
    if (language == DEFAULT_LANGUAGE) return emptyList()
    return translationsRepository.loadTranslations(season, show.ids.trakt, language, onlyLocal)
  }
}
