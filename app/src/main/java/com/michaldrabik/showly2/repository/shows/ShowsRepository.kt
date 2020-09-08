package com.michaldrabik.showly2.repository.shows

import com.michaldrabik.showly2.di.scope.AppScope
import javax.inject.Inject

@AppScope
class ShowsRepository @Inject constructor(
  val discoverShows: DiscoverShowsRepository,
  val myShows: MyShowsRepository,
  val seeLaterShows: SeeLaterShowsRepository,
  val archiveShows: ArchiveShowsRepository,
  val relatedShows: RelatedShowsRepository,
  val detailsShow: ShowDetailsRepository
)
