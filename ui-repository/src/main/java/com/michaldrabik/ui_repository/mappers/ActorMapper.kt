package com.michaldrabik.ui_repository.mappers

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.network.tmdb.model.TmdbActor
import com.michaldrabik.network.tvdb.model.TvdbActor
import com.michaldrabik.ui_model.Actor
import javax.inject.Inject
import com.michaldrabik.storage.database.model.Actor as ActorDb

class ActorMapper @Inject constructor() {

  fun fromNetwork(actor: TvdbActor) = Actor(
    id = actor.id ?: -1,
    imdbId = null,
    tvdbShowId = actor.seriesId ?: -1,
    tmdbMovieId = -1,
    name = actor.name ?: "",
    role = actor.role ?: "",
    sortOrder = actor.sortOrder ?: -1,
    image = actor.image ?: ""
  )

  fun fromNetwork(actor: TmdbActor) = Actor(
    id = actor.id,
    imdbId = null,
    tvdbShowId = -1,
    tmdbMovieId = actor.movieTmdbId,
    name = actor.name ?: "",
    role = actor.character ?: "",
    sortOrder = actor.order,
    image = actor.profile_path ?: ""
  )

  fun fromDatabase(actor: ActorDb) = Actor(
    id = actor.idTvdb,
    imdbId = actor.idImdb,
    tvdbShowId = actor.idShowTvdb,
    tmdbMovieId = actor.idMovieTmdb,
    name = actor.name,
    role = actor.role,
    sortOrder = actor.sortOrder,
    image = actor.image
  )

  fun toDatabase(actor: Actor) = ActorDb(
    0,
    idTvdb = actor.id,
    idImdb = actor.imdbId,
    idShowTvdb = actor.tvdbShowId,
    idMovieTmdb = actor.tmdbMovieId,
    name = actor.name,
    role = actor.role,
    sortOrder = actor.sortOrder,
    image = actor.image,
    createdAt = nowUtcMillis(),
    updatedAt = nowUtcMillis()
  )
}
