package com.michaldrabik.data_remote.trakt.model

data class PersonCreditsResult(
  val cast: List<PersonCredit>?,
  val crew: Map<String, List<PersonCredit>>?,
)
