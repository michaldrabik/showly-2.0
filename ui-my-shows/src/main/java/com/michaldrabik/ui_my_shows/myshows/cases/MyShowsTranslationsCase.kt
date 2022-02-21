package com.michaldrabik.ui_my_shows.myshows.cases

import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MyShowsTranslationsCase @Inject constructor(
  private val translationsRepository: TranslationsRepository,
) {

  val language by lazy { translationsRepository.getLanguage() }

  suspend fun loadTranslation(show: Show, onlyLocal: Boolean): Translation? {
    if (language == Config.DEFAULT_LANGUAGE) return Translation.EMPTY
    return translationsRepository.loadTranslation(show, language, onlyLocal)
  }
}
