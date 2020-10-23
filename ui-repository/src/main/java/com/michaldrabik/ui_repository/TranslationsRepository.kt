package com.michaldrabik.ui_repository

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.network.Cloud
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.ShowTranslation
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_repository.mappers.Mappers
import java.util.*
import javax.inject.Inject

@AppScope
class TranslationsRepository @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun loadTranslation(show: Show, locale: Locale = Locale.ENGLISH): Translation? {
    val local = database.showTranslationsDao().getById(show.traktId, locale.language)
    local?.let {
      return mappers.translation.fromDatabase(it).copy(isLocal = true)
    }

    val remoteTranslation = cloud.traktApi.fetchShowTranslations(show.traktId, locale.language).firstOrNull()
    val translation = mappers.translation.fromNetwork(remoteTranslation)
    val translationDb = ShowTranslation.fromTraktId(
      show.traktId,
      translation.title,
      translation.language,
      translation.overview,
      nowUtcMillis()
    )

    if (translationDb.overview.isNotBlank()) {
      database.showTranslationsDao().insert(translationDb)
    }

    return translation
  }
}
