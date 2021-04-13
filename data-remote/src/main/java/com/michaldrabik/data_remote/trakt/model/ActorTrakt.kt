package com.michaldrabik.data_remote.trakt.model

data class ActorTrakt(
  val person: Person?
)

data class Person(
  val ids: Ids?,
  val name: String?
)
