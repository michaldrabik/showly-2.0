package com.michaldrabik.ui_repository.mappers

import com.michaldrabik.storage.database.model.ShowTranslation
import com.michaldrabik.ui_model.Translation
import javax.inject.Inject
import com.michaldrabik.network.trakt.model.Translation as TranslationNetwork

class TranslationMapper @Inject constructor() {

  fun fromNetwork(value: TranslationNetwork?) =
    Translation(
      title = value?.title ?: "",
      overview = value?.overview ?: "",
      language = value?.language ?: ""
    )

  fun fromDatabase(value: ShowTranslation?) =
    Translation(
      title = value?.title ?: "",
      overview = value?.overview ?: "",
      language = value?.language ?: ""
    )
}
