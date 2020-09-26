package com.michaldrabik.ui_repository.mappers

import javax.inject.Inject

@com.michaldrabik.common.di.AppScope
class Mappers @Inject constructor(
  val ids: IdsMapper,
  val image: ImageMapper,
  val show: ShowMapper,
  val episode: EpisodeMapper,
  val season: SeasonMapper,
  val actor: ActorMapper,
  val comment: CommentMapper,
  val settings: SettingsMapper
)
