package com.michaldrabik.repository

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.data_remote.tmdb.model.TmdbStreamingCountry
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.StreamingService
import com.michaldrabik.ui_model.StreamingService.Option.ADS
import com.michaldrabik.ui_model.StreamingService.Option.BUY
import com.michaldrabik.ui_model.StreamingService.Option.FLATRATE
import com.michaldrabik.ui_model.StreamingService.Option.FREE
import com.michaldrabik.ui_model.StreamingService.Option.RENT
import javax.inject.Inject

@AppScope
class StreamingsRepository @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
) {

  suspend fun loadStreamings(movie: Movie, countryCode: String): List<StreamingService> {
    val remoteItems = cloud.tmdbApi.fetchMovieWatchProviders(movie.ids.tmdb.id, countryCode) ?: return emptyList()
    return processRemoteItems(remoteItems, movie.title, countryCode)
  }

  suspend fun loadStreamings(show: Show, countryCode: String): List<StreamingService> {
    val remoteItems = cloud.tmdbApi.fetchShowWatchProviders(show.ids.tmdb.id, countryCode) ?: return emptyList()
    return processRemoteItems(remoteItems, show.title, countryCode)
  }

  private fun processRemoteItems(
    remoteItems: TmdbStreamingCountry,
    mediaName: String,
    countryCode: String,
  ): List<StreamingService> {
    val items = mutableListOf<StreamingService>()
    remoteItems.flatrate?.forEach { flatrate ->
      val item = StreamingService(
        imagePath = flatrate.logo_path,
        name = flatrate.provider_name,
        options = listOf(FLATRATE),
        link = remoteItems.link,
        mediaName = mediaName,
        countryCode = countryCode
      )
      items.add(item)
    }
    remoteItems.free?.forEach { flatrate ->
      val item = StreamingService(
        imagePath = flatrate.logo_path,
        name = flatrate.provider_name,
        options = listOf(FREE),
        link = remoteItems.link,
        mediaName = mediaName,
        countryCode = countryCode
      )
      items.add(item)
    }
    remoteItems.buy?.forEach { flatrate ->
      val item = StreamingService(
        imagePath = flatrate.logo_path,
        name = flatrate.provider_name,
        options = listOf(BUY),
        link = remoteItems.link,
        mediaName = mediaName,
        countryCode = countryCode
      )
      items.add(item)
    }
    remoteItems.rent?.forEach { flatrate ->
      val item = StreamingService(
        imagePath = flatrate.logo_path,
        name = flatrate.provider_name,
        options = listOf(RENT),
        link = remoteItems.link,
        mediaName = mediaName,
        countryCode = countryCode
      )
      items.add(item)
    }
    remoteItems.ads?.forEach { flatrate ->
      val item = StreamingService(
        imagePath = flatrate.logo_path,
        name = flatrate.provider_name,
        options = listOf(ADS),
        link = remoteItems.link,
        mediaName = mediaName,
        countryCode = countryCode
      )
      items.add(item)
    }
    return items
      .groupBy { it.name }
      .filter { it.value.isNotEmpty() }
      .map { entry ->
        val entryValue = entry.value.first()
        StreamingService(
          name = entry.key,
          imagePath = entryValue.imagePath,
          options = entry.value.flatMap { it.options },
          link = entryValue.link,
          mediaName = entryValue.mediaName,
          countryCode = countryCode
        )
      }
  }
}
