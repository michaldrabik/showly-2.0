package com.michaldrabik.ui_show.cases

import com.michaldrabik.common.Config.ACTORS_CACHE_DURATION
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Actor
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import java.util.Locale.ENGLISH
import javax.inject.Inject

@ViewModelScoped
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

    val remoteTraktActors = cloud.traktApi.fetchShowActors(show.traktId)
    val remoteTmdbActors = cloud.tmdbApi.fetchShowActors(tmdbId)
      .asSequence()
      .distinctBy { (it.name + it.character).lowercase(ENGLISH) }
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
