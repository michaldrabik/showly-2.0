package com.michaldrabik.repository.shows

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowsRepository @Inject constructor(
  val discoverShows: DiscoverShowsRepository,
  val myShows: MyShowsRepository,
  val watchlistShows: WatchlistShowsRepository,
  val hiddenShows: HiddenShowsRepository,
  val relatedShows: RelatedShowsRepository,
  val detailsShow: ShowDetailsRepository
)
