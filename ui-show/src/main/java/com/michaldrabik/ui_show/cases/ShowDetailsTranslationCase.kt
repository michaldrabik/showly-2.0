package com.michaldrabik.ui_show.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_model.locale.AppLocale
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsTranslationCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val translationsRepository: TranslationsRepository
) {

  suspend fun loadTranslation(show: Show): Translation? =
    withContext(dispatchers.IO) {
      val locale = translationsRepository.getLocale()
      if (locale == AppLocale.default()) {
        return@withContext null
      }
      translationsRepository.loadTranslation(show, locale)
    }
}
