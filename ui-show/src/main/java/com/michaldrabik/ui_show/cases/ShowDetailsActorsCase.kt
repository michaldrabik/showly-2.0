package com.michaldrabik.ui_show.cases

import com.michaldrabik.common.Config.ACTORS_CACHE_DURATION
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.network.Cloud
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_model.Actor
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_repository.mappers.Mappers
import java.util.Locale.ENGLISH
import javax.inject.Inject

@AppScope
class ShowDetailsActorsCase @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun loadActors(show: Show): List<Actor> {
    val tmdbId = show.ids.tmdb.id
    if (tmdbId == -1L) return emptyList()

    val localActors = database.actorsDao().getAllByShow(tmdbId)
    if (localActors.isNotEmpty() && nowUtcMillis() - localActors[0].updatedAt < ACTORS_CACHE_DURATION) {
      return localActors
        .sortedWith(compareBy({ it.image.isBlank() }, { it.sortOrder }))
        .map { mappers.actor.fromDatabase(it) }
    }

    val remoteTraktActors = cloud.traktApi.fetchShowActors(show.ids.trakt.id)
    val remoteTmdbActors = cloud.tmdbApi.fetchShowActors(tmdbId)
      .asSequence()
      .distinctBy { (it.name + it.character).toLowerCase(ENGLISH) }
      .sortedWith(compareBy({ it.profile_path.isNullOrBlank() }, { it.order }))
      .take(30)
      .map { mappers.actor.fromNetwork(it) }
      .map {
        val imdbId = remoteTraktActors.find { actor -> actor.person?.name?.equals(it.name, true) == true }?.person?.ids?.imdb
        it.copy(imdbId = imdbId)
      }
      .toList()

    database.actorsDao().replaceForShow(remoteTmdbActors.map { mappers.actor.toDatabase(it) }, tmdbId)
    return remoteTmdbActors
  }
}
