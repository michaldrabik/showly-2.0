package com.michaldrabik.repository.mappers

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Mappers @Inject constructor(
  val ids: IdsMapper,
  val image: ImageMapper,
  val show: ShowMapper,
  val movie: MovieMapper,
  val episode: EpisodeMapper,
  val season: SeasonMapper,
  val person: PersonMapper,
  val comment: CommentMapper,
  val news: NewsMapper,
  val settings: SettingsMapper,
  val translation: TranslationMapper,
  val customList: CustomListMapper,
  val ratings: RatingsMapper,
  val userRatings: UserRatingsMapper,
  val streamings: StreamingsMapper,
)
