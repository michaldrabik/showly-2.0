package com.michaldrabik.ui_repository

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_repository.mappers.Mappers
import java.util.*
import javax.inject.Inject

@AppScope
class TranslationsRepository @Inject constructor(
  private val cloud: Cloud,
  private val mappers: Mappers
) {

  suspend fun loadTranslation(show: Show, locale: Locale = Locale.ENGLISH): Translation? {
    val remoteTranslation = cloud.traktApi.fetchShowTranslations(show.traktId, locale.language).firstOrNull()
    return remoteTranslation?.let { mappers.translation.fromNetwork(it) }
  }
}
