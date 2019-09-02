package com.michaldrabik.network.trakt.converters

import com.michaldrabik.network.trakt.model.Episode
import com.michaldrabik.network.trakt.model.Ids
import com.michaldrabik.network.trakt.model.json.EpisodeJson
import com.squareup.moshi.FromJson

class EpisodeConverter {

  @FromJson
  fun fromJson(json: EpisodeJson) =
    Episode(
      json.season ?: -1,
      json.number ?: -1,
      json.title ?: "",
      Ids(
        json.ids?.trakt ?: -1,
        json.ids?.tvdb ?: -1,
        json.ids?.tmdb ?: -1,
        json.ids?.tvrage ?: -1,
        json.ids?.imdb ?: "",
        json.ids?.slug ?: ""
      ),
      json.overview ?: "",
      json.rating ?: 0F,
      json.votes ?: 0,
      json.comment_count ?: 0,
      json.first_aired ?: "",
      json.updated_at ?: "",
      json.runtime ?: -1
    )
}