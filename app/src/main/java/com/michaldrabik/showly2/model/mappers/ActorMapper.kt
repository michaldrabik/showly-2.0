package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.network.tvdb.model.TvdbActor
import com.michaldrabik.showly2.model.Actor
import javax.inject.Inject

class ActorMapper @Inject constructor() {

  fun fromNetwork(actor: TvdbActor) = Actor(
    actor.id,
    actor.tvdbShowId,
    actor.name,
    actor.role,
    actor.sortOrder,
    actor.image
  )
}