package com.michaldrabik.network.trakt.converters

import com.michaldrabik.network.trakt.model.SearchResult
import com.michaldrabik.network.trakt.model.json.SearchResultJson
import com.squareup.moshi.FromJson

class SearchResultConverter(
  private val showConverter: ShowConverter
) {

  @FromJson
  fun fromJson(json: SearchResultJson) =
    SearchResult(
      json.score ?: 0F,
      showConverter.fromJson(json.show!!)
    )
}