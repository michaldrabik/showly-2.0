package com.michaldrabik.ui_search.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class SearchTranslationsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val translationsRepository: TranslationsRepository,
) {

  fun getLanguage() = translationsRepository.getLanguage()

  suspend fun loadTranslation(show: Show): Translation? = withContext(dispatchers.IO) {
    val language = getLanguage()
    if (language == Config.DEFAULT_LANGUAGE) {
      return@withContext Translation.EMPTY
    }
    translationsRepository.loadTranslation(show, language)
  }

  suspend fun loadTranslation(movie: Movie): Translation? = withContext(dispatchers.IO) {
    val language = getLanguage()
    if (language == Config.DEFAULT_LANGUAGE) {
      return@withContext Translation.EMPTY
    }
    translationsRepository.loadTranslation(movie, language)
  }
}
