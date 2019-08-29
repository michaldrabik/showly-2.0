package com.michaldrabik.network.tvdb.converters

import com.michaldrabik.network.tvdb.model.TvdbImagesResult
import com.michaldrabik.network.tvdb.model.json.TvdbImagesResultJson
import com.squareup.moshi.FromJson

class TvdbImagesResultConverter(
  private val tvdbImageConverter: TvdbImageConverter
) {

  @FromJson
  fun fromJson(json: TvdbImagesResultJson) =
    TvdbImagesResult(
      json.data?.map { tvdbImageConverter.fromJson(it) } ?: emptyList()
    )
}