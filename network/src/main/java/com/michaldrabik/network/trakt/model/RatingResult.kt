package com.michaldrabik.network.trakt.model

import com.michaldrabik.network.trakt.model.json.IdsJson

data class RatingResult(
  val rating: Int,
  val show: RatingResultShow
)

data class RatingResultShow(
  val title: String,
  val ids: IdsJson
)
