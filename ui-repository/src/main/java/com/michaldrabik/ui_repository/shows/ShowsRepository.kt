package com.michaldrabik.ui_repository.shows

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.Show
import javax.inject.Inject

@AppScope
class ShowsRepository @Inject constructor(
  val discoverShows: DiscoverShowsRepository,
  val myShows: MyShowsRepository,
  val seeLaterShows: SeeLaterShowsRepository,
  val archiveShows: ArchiveShowsRepository,
  val relatedShows: RelatedShowsRepository,
  val detailsShow: ShowDetailsRepository
) {

  suspend fun loadCollection(): List<Show> {
    val myShows = myShows.loadAll()
    val seeLaterShows = seeLaterShows.loadAll()
    val archivedShows = archiveShows.loadAll()
    return (myShows + seeLaterShows + archivedShows).distinctBy { it.traktId }
  }
}
