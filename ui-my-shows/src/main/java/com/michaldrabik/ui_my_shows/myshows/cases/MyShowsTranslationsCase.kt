package com.michaldrabik.ui_my_shows.myshows.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_model.locale.AppLocale
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MyShowsTranslationsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val translationsRepository: TranslationsRepository,
) {

  fun getLocale() = translationsRepository.getLocale()

  suspend fun loadTranslation(show: Show, onlyLocal: Boolean): Translation? =
    withContext(dispatchers.IO) {
      val locale = getLocale()
      if (locale == AppLocale.default()) {
        return@withContext Translation.EMPTY
      }
      translationsRepository.loadTranslation(show, locale, onlyLocal)
    }
}
