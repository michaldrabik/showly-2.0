package com.michaldrabik.network.trakt.converters

import com.michaldrabik.network.trakt.model.TrendingResult
import com.michaldrabik.network.trakt.model.json.TrendingResultJson
import com.squareup.moshi.FromJson

class TrendingResultConverter(
  private val showConverter: ShowConverter
) {

  @FromJson
  fun fromJson(json: TrendingResultJson) =
    TrendingResult(
      showConverter.fromJson(json.show!!)
    )
}