package com.michaldrabik.network.tvdb.converters

import com.michaldrabik.network.tvdb.model.TvdbActor
import com.michaldrabik.network.tvdb.model.json.TvdbActorJson
import com.squareup.moshi.FromJson

class TvdbActorConverter : TvdbDataConverter<TvdbActorJson, TvdbActor> {

  @FromJson
  override fun fromJson(json: TvdbActorJson) =
    TvdbActor(
      json.id ?: -1,
      json.seriesId ?: -1,
      json.name ?: "",
      json.role ?: "",
      json.sortOrder ?: -1,
      json.image ?: ""
    )
}