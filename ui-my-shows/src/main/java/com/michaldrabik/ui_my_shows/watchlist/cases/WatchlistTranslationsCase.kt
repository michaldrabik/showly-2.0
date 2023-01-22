package com.michaldrabik.ui_my_shows.watchlist.cases

import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class WatchlistTranslationsCase @Inject constructor(
  private val translationsRepository: TranslationsRepository,
) {

  fun getLanguage() = translationsRepository.getLanguage()

  suspend fun loadTranslation(show: Show, onlyLocal: Boolean): Translation? {
    val language = getLanguage()
    if (language == Config.DEFAULT_LANGUAGE) {
      return Translation.EMPTY
    }
    return translationsRepository.loadTranslation(show, language, onlyLocal)
  }
}
