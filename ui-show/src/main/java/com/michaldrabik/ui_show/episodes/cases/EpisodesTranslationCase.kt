package com.michaldrabik.ui_show.episodes.cases

import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.SeasonTranslation
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class EpisodesTranslationCase @Inject constructor(
  private val translationsRepository: TranslationsRepository
) {

  suspend fun loadTranslations(season: Season?, show: Show): List<SeasonTranslation> {
    if (season == null) {
      return emptyList()
    }

    val language = translationsRepository.getLanguage()
    if (language == DEFAULT_LANGUAGE) {
      return emptyList()
    }

    return translationsRepository.loadTranslations(season, show.ids.trakt, language)
  }
}
