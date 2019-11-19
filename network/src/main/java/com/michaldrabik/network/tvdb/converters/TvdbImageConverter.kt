package com.michaldrabik.network.tvdb.converters

import com.michaldrabik.network.tvdb.model.TvdbImage
import com.michaldrabik.network.tvdb.model.TvdbImageRating
import com.michaldrabik.network.tvdb.model.json.TvdbImageJson
import com.squareup.moshi.FromJson

class TvdbImageConverter : TvdbDataConverter<TvdbImageJson, TvdbImage> {

  @FromJson
  override fun fromJson(json: TvdbImageJson) =
    TvdbImage(
      json.id ?: -1,
      json.fileName ?: "",
      json.thumbnail ?: "",
      json.keyType ?: "",
      TvdbImageRating(
        json.ratingsInfo?.average ?: 0F,
        json.ratingsInfo?.count ?: 0
      )
    )
}