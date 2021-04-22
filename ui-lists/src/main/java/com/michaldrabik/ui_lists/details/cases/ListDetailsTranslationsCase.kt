package com.michaldrabik.ui_lists.details.cases

import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import com.michaldrabik.ui_model.Translation
import javax.inject.Inject

class ListDetailsTranslationsCase @Inject constructor(
  private val translationsRepository: TranslationsRepository
) {

  val language by lazy { translationsRepository.getLanguage() }

  suspend fun loadTranslation(item: ListDetailsItem, onlyLocal: Boolean): Translation? {
    if (language == Config.DEFAULT_LANGUAGE) return Translation.EMPTY
    return when {
      item.isShow() -> translationsRepository.loadTranslation(item.requireShow(), language, onlyLocal)
      item.isMovie() -> translationsRepository.loadTranslation(item.requireMovie(), language, onlyLocal)
      else -> throw IllegalStateException()
    }
  }
}
