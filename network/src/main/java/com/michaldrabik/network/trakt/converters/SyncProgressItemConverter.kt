package com.michaldrabik.network.trakt.converters

import com.michaldrabik.network.trakt.model.SyncProgressItem
import com.michaldrabik.network.trakt.model.json.SyncProgressItemJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class SyncProgressItemConverter(
  private val showConverter: ShowConverter,
  private val seasonConverter: SeasonConverter
) {

  @FromJson
  fun fromJson(json: SyncProgressItemJson) =
    SyncProgressItem(
      show = showConverter.fromJson(json.show!!),
      seasons = json.seasons?.map { seasonConverter.fromJson(it) } ?: emptyList()
    )

  @ToJson
  fun toJson(value: SyncProgressItem): SyncProgressItemJson = throw UnsupportedOperationException()
}