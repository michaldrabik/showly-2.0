package com.michaldrabik.network.trakt.converters

import com.michaldrabik.network.trakt.model.Ids
import com.michaldrabik.network.trakt.model.Season
import com.michaldrabik.network.trakt.model.json.SeasonJson
import com.squareup.moshi.FromJson

class SeasonConverter(
  private val episodeConverter: EpisodeConverter
) {

  @FromJson
  fun fromJson(json: SeasonJson) =
    Season(
      Ids(
        json.ids?.trakt ?: -1,
        json.ids?.tvdb ?: -1,
        json.ids?.tmdb ?: -1,
        json.ids?.tvrage ?: -1,
        json.ids?.imdb ?: "",
        json.ids?.slug ?: ""
      ),
      json.number ?: -1,
      json.episode_count ?: -1,
      json.aired_episodes ?: -1,
      json.title ?: "",
      json.first_aired ?: "",
      json.overview ?: "",
      json.episodes?.map {
        episodeConverter.fromJson(it)
      } ?: emptyList()
    )
}