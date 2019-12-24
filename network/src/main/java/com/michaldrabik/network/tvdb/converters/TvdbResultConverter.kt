package com.michaldrabik.network.tvdb.converters

import com.michaldrabik.network.tvdb.model.TvdbResult
import com.michaldrabik.network.tvdb.model.json.TvdbResultJson
import com.squareup.moshi.FromJson

class TvdbResultConverter<Json, Result>(
  private val converter: TvdbDataConverter<Json, Result>
) {

  @FromJson
  fun fromJson(json: TvdbResultJson<Json>) =
    TvdbResult(
      json.data?.map { converter.fromJson(it) } ?: emptyList()
    )
}
