package com.michaldrabik.ui_repository.shows

import com.michaldrabik.common.di.AppScope
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
