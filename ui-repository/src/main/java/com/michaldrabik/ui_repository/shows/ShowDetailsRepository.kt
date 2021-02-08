package com.michaldrabik.ui_repository.shows

import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.network.Cloud
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_repository.mappers.Mappers
import javax.inject.Inject

@AppScope
class ShowDetailsRepository @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers
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

  suspend fun loadComments(idTrakt: IdTrakt, limit: Int) =
    cloud.traktApi.fetchShowComments(idTrakt.id, limit)
      .map { mappers.comment.fromNetwork(it) }
}
