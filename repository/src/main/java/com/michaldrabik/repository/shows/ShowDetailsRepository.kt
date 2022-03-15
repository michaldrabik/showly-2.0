package com.michaldrabik.repository.shows

import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.IdImdb
import com.michaldrabik.ui_model.IdSlug
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Show
import javax.inject.Inject

class ShowDetailsRepository @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val database: AppDatabase,
  private val mappers: Mappers,
) {

  suspend fun load(idTrakt: IdTrakt, force: Boolean = false): Show {
    val localShow = database.showsDao().getById(idTrakt.id)
    if (force || localShow == null || nowUtcMillis() - localShow.updatedAt > Config.SHOW_DETAILS_CACHE_DURATION) {
      val remoteShow = remoteSource.trakt.fetchShow(idTrakt.id)
      val show = mappers.show.fromNetwork(remoteShow)
      database.showsDao().upsert(listOf(mappers.show.toDatabase(show)))
      return show
    }
    return mappers.show.fromDatabase(localShow)
  }

  suspend fun find(idImdb: IdImdb): Show? {
    val localShow = database.showsDao().getById(idImdb.id)
    if (localShow != null) {
      return mappers.show.fromDatabase(localShow)
    }
    return null
  }

  suspend fun find(idTmdb: IdTmdb): Show? {
    val localShow = database.showsDao().getByTmdbId(idTmdb.id)
    if (localShow != null) {
      return mappers.show.fromDatabase(localShow)
    }
    return null
  }

  suspend fun find(idSlug: IdSlug): Show? {
    val localShow = database.showsDao().getBySlug(idSlug.id)
    if (localShow != null) {
      return mappers.show.fromDatabase(localShow)
    }
    return null
  }

  suspend fun delete(idTrakt: IdTrakt) {
    with(database) {
      showsDao().deleteById(idTrakt.id)
      seasonsDao().deleteAllForShow(idTrakt.id)
      episodesDao().deleteAllForShow(idTrakt.id)
    }
  }
}
