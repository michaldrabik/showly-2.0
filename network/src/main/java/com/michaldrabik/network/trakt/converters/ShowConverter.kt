package com.michaldrabik.network.trakt.converters

import com.michaldrabik.network.trakt.model.AirTime
import com.michaldrabik.network.trakt.model.Ids
import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.network.trakt.model.json.ShowJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class ShowConverter {

  @FromJson
  fun fromJson(json: ShowJson) =
    Show(
      Ids(
        json.ids?.trakt ?: -1,
        json.ids?.tvdb ?: -1,
        json.ids?.tmdb ?: -1,
        json.ids?.tvrage ?: -1,
        json.ids?.imdb ?: "",
        json.ids?.slug ?: ""
      ),
      json.title ?: "",
      json.year ?: -1,
      json.overview ?: "",
      json.first_aired ?: "",
      json.runtime ?: -1,
      AirTime(
        json.airs?.day ?: "",
        json.airs?.time ?: "",
        json.airs?.timezone ?: ""
      ),
      json.certification ?: "",
      json.network ?: "",
      json.country ?: "",
      json.trailer ?: "",
      json.homepage ?: "",
      json.status ?: "",
      json.rating ?: -1F,
      json.votes ?: -1,
      json.comment_count ?: -1,
      json.genres?.map { it } ?: emptyList(),
      json.aired_episodes ?: -1
    )

  @ToJson
  fun toJson(value: Show): ShowJson = throw UnsupportedOperationException()
}
