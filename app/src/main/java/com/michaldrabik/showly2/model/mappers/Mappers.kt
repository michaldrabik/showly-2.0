package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.di.scope.AppScope
import javax.inject.Inject

@AppScope
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
