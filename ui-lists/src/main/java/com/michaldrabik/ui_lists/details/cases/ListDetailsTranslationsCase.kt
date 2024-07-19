package com.michaldrabik.ui_lists.details.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_model.locale.AppLocale
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ListDetailsTranslationsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val translationsRepository: TranslationsRepository,
) {

  fun getLocale() = translationsRepository.getLocale()

  suspend fun loadTranslation(item: ListDetailsItem, onlyLocal: Boolean): Translation? =
    withContext(dispatchers.IO) {
      val locale = getLocale()
      if (locale == AppLocale.default()) {
        return@withContext Translation.EMPTY
      }
      when {
        item.isShow() ->
          translationsRepository.loadTranslation(
            show = item.requireShow(),
            locale = locale,
            onlyLocal = onlyLocal
          )
        item.isMovie() ->
          translationsRepository.loadTranslation(
            movie = item.requireMovie(),
            locale = locale,
            onlyLocal = onlyLocal
          )
        else -> throw IllegalStateException()
      }
    }
}
