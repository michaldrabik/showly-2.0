package com.michaldrabik.network.trakt.converters

import com.michaldrabik.network.trakt.model.ShowResult
import com.michaldrabik.network.trakt.model.json.TrendingResultJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class TrendingResultConverter(
  private val showConverter: ShowConverter
) {

  @FromJson
  fun fromJson(json: TrendingResultJson) =
    ShowResult(
      showConverter.fromJson(json.show!!)
    )

  @ToJson
  fun toJson(value: ShowResult): TrendingResultJson = throw UnsupportedOperationException()
}
