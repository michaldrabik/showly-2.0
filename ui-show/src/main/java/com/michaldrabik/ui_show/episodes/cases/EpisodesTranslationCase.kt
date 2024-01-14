package com.michaldrabik.ui_show.episodes.cases

import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.SeasonTranslation
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class EpisodesTranslationCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val translationsRepository: TranslationsRepository
) {

  suspend fun loadTranslations(season: Season?, show: Show): List<SeasonTranslation> =
    withContext(dispatchers.IO) {
      if (season == null) {
        return@withContext emptyList()
      }

      val language = translationsRepository.getLanguage()
      if (language == DEFAULT_LANGUAGE) {
        return@withContext emptyList()
      }

      translationsRepository.loadTranslations(season, show.ids.trakt, language)
    }
}
