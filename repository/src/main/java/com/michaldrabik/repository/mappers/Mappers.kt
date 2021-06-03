package com.michaldrabik.repository.mappers

import com.michaldrabik.common.di.AppScope
import javax.inject.Inject

@AppScope
class Mappers @Inject constructor(
  val ids: IdsMapper,
  val image: ImageMapper,
  val show: ShowMapper,
  val movie: MovieMapper,
  val episode: EpisodeMapper,
  val season: SeasonMapper,
  val actor: ActorMapper,
  val comment: CommentMapper,
  val news: NewsMapper,
  val settings: SettingsMapper,
  val translation: TranslationMapper,
  val customList: CustomListMapper,
  val ratings: RatingsMapper,
  val streamings: StreamingsMapper,
)
