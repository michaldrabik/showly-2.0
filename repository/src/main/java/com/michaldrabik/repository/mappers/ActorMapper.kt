package com.michaldrabik.repository.mappers

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_remote.tmdb.model.TmdbActor
import com.michaldrabik.ui_model.Actor
import javax.inject.Inject
import com.michaldrabik.data_local.database.model.Actor as ActorDb

class ActorMapper @Inject constructor() {

  fun fromNetwork(actor: TmdbActor) = Actor(
    tvdbId = -1,
    tmdbId = actor.id,
    imdbId = null,
    tvdbShowId = -1,
    tmdbShowId = actor.showTmdbId,
    tmdbMovieId = actor.movieTmdbId,
    name = actor.name ?: "",
    role = actor.character ?: "",
    sortOrder = actor.order,
    image = actor.profile_path ?: ""
  )

  fun fromDatabase(actor: ActorDb) = Actor(
    tvdbId = actor.idTvdb,
    tmdbId = actor.idTmdb,
    imdbId = actor.idImdb,
    tvdbShowId = actor.idShowTvdb,
    tmdbMovieId = actor.idMovieTmdb,
    tmdbShowId = actor.idShowTmdb,
    name = actor.name,
    role = actor.role,
    sortOrder = actor.sortOrder,
    image = actor.image
  )

  fun toDatabase(actor: Actor) = ActorDb(
    0,
    idTvdb = actor.tvdbId,
    idTmdb = actor.tmdbId,
    idImdb = actor.imdbId,
    idShowTvdb = actor.tvdbShowId,
    idShowTmdb = actor.tmdbShowId,
    idMovieTmdb = actor.tmdbMovieId,
    name = actor.name,
    role = actor.role,
    sortOrder = actor.sortOrder,
    image = actor.image,
    createdAt = nowUtcMillis(),
    updatedAt = nowUtcMillis()
  )
}
