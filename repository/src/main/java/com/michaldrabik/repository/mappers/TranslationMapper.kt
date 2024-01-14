package com.michaldrabik.repository.mappers

import com.michaldrabik.data_local.database.model.EpisodeTranslation
import com.michaldrabik.data_local.database.model.MovieTranslation
import com.michaldrabik.data_local.database.model.ShowTranslation
import com.michaldrabik.ui_model.SeasonTranslation
import com.michaldrabik.ui_model.Translation
import javax.inject.Inject
import com.michaldrabik.data_remote.trakt.model.SeasonTranslation as SeasonTranslationNetwork
import com.michaldrabik.data_remote.trakt.model.Translation as TranslationNetwork

class TranslationMapper @Inject constructor(
  private val idsMapper: IdsMapper
) {

  fun fromNetwork(value: TranslationNetwork?) =
    Translation(
      title = value?.title ?: "",
      overview = value?.overview ?: "",
      language = value?.language ?: ""
    )

  fun fromNetwork(value: SeasonTranslationNetwork?) =
    SeasonTranslation(
      ids = idsMapper.fromNetwork(value?.ids),
      seasonNumber = value?.season ?: -1,
      episodeNumber = value?.number ?: -1,
      title = value?.translations?.firstOrNull()?.title ?: "",
      overview = value?.translations?.firstOrNull()?.overview ?: "",
      language = value?.translations?.firstOrNull()?.language ?: ""
    )

  fun fromDatabase(value: ShowTranslation?) =
    Translation(
      title = value?.title ?: "",
      overview = value?.overview ?: "",
      language = value?.language ?: ""
    )

  fun fromDatabase(value: MovieTranslation?) =
    Translation(
      title = value?.title ?: "",
      overview = value?.overview ?: "",
      language = value?.language ?: ""
    )

  fun fromDatabase(value: EpisodeTranslation?) =
    Translation(
      title = value?.title ?: "",
      overview = value?.overview ?: "",
      language = value?.language ?: ""
    )
}
