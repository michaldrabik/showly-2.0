package com.michaldrabik.network.tvdb.converters

import com.michaldrabik.network.tvdb.model.TvdbImage
import com.michaldrabik.network.tvdb.model.json.TvdbImageJson
import com.squareup.moshi.FromJson

class TvdbImageConverter {

  @FromJson
  fun fromJson(json: TvdbImageJson) =
    TvdbImage(
      json.id ?: -1,
      json.fileName ?: "",
      json.thumbnail ?: ""
    )
}