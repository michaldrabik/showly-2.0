package com.michaldrabik.network.trakt.converters

import com.michaldrabik.network.trakt.model.SearchResult
import com.michaldrabik.network.trakt.model.json.SearchResultJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class SearchResultConverter(
  private val showConverter: ShowConverter
) {

  @FromJson
  fun fromJson(json: SearchResultJson) =
    SearchResult(
      json.score ?: 0F,
      showConverter.fromJson(json.show!!)
    )

  @ToJson
  fun toJson(value: SearchResult): SearchResultJson = throw UnsupportedOperationException()
}
