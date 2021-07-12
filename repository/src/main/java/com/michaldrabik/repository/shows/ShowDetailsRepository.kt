package com.michaldrabik.repository.shows

import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Show
import javax.inject.Inject

class ShowDetailsRepository @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
) {

  suspend fun load(idTrakt: IdTrakt, force: Boolean = false): Show {
    val localShow = database.showsDao().getById(idTrakt.id)
    if (force || localShow == null || nowUtcMillis() - localShow.updatedAt > Config.SHOW_DETAILS_CACHE_DURATION) {
      val remoteShow = cloud.traktApi.fetchShow(idTrakt.id)
      val show = mappers.show.fromNetwork(remoteShow)
      database.showsDao().upsert(listOf(mappers.show.toDatabase(show)))
      return show
    }
    return mappers.show.fromDatabase(localShow)
  }

  suspend fun delete(idTrakt: IdTrakt) {
    with(database) {
      showsDao().deleteById(idTrakt.id)
      seasonsDao().deleteAllForShow(idTrakt.id)
      episodesDao().deleteAllForShow(idTrakt.id)
    }
  }
}
