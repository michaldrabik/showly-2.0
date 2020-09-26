package com.michaldrabik.showly2.ui.show.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.Config.ACTORS_CACHE_DURATION
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_model.Actor
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_repository.UserTvdbManager
import com.michaldrabik.ui_repository.mappers.Mappers
import java.util.Locale.ENGLISH
import javax.inject.Inject

@AppScope
class ShowDetailsActorsCase @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val userTvdbManager: UserTvdbManager,
  private val mappers: Mappers
) {

  suspend fun loadActors(show: Show): List<Actor> {
    val localActors = database.actorsDao().getAllByShow(show.ids.tvdb.id)
    if (localActors.isNotEmpty() && nowUtcMillis() - localActors[0].updatedAt < ACTORS_CACHE_DURATION) {
      return localActors
        .sortedWith(compareBy({ it.image.isBlank() }, { it.sortOrder }))
        .map { mappers.actor.fromDatabase(it) }
    }

    userTvdbManager.checkAuthorization()
    val token = userTvdbManager.getToken()

    val remoteTraktActors = cloud.traktApi.fetchShowActors(show.ids.trakt.id)
    val remoteTmdbActors = cloud.tvdbApi.fetchActors(token, show.ids.tvdb.id)
      .asSequence()
      .distinctBy { (it.name + it.role).toLowerCase(ENGLISH) }
      .sortedWith(compareBy({ it.image.isNullOrBlank() }, { it.sortOrder }))
      .take(20)
      .map { mappers.actor.fromNetwork(it) }
      .map {
        val imdbId = remoteTraktActors.find { actor -> actor.person?.name?.equals(it.name, true) == true }?.person?.ids?.imdb
        it.copy(imdbId = imdbId)
      }
      .toList()

    database.actorsDao().replace(remoteTmdbActors.map { mappers.actor.toDatabase(it) }, show.ids.tvdb.id)
    return remoteTmdbActors
  }
}
