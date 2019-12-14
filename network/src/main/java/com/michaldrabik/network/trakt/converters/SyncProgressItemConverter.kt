package com.michaldrabik.network.trakt.converters

import com.michaldrabik.network.trakt.model.SyncItem
import com.michaldrabik.network.trakt.model.json.SyncItemJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class SyncProgressItemConverter(
  private val showConverter: ShowConverter,
  private val seasonConverter: SeasonConverter
) {

  @FromJson
  fun fromJson(json: SyncItemJson) =
    SyncItem(
      show = json.show?.let { showConverter.fromJson(it) },
      seasons = json.seasons?.map { seasonConverter.fromJson(it) } ?: emptyList()
    )

  @ToJson
  fun toJson(value: SyncItem): SyncItemJson = throw UnsupportedOperationException()
}