package com.michaldrabik.repository

import com.michaldrabik.data_remote.tmdb.model.TmdbStreamingCountry
import com.michaldrabik.data_remote.tmdb.model.TmdbStreamingService
import com.michaldrabik.ui_model.StreamingService
import com.michaldrabik.ui_model.StreamingService.Option.ADS
import com.michaldrabik.ui_model.StreamingService.Option.BUY
import com.michaldrabik.ui_model.StreamingService.Option.FLATRATE
import com.michaldrabik.ui_model.StreamingService.Option.FREE
import com.michaldrabik.ui_model.StreamingService.Option.RENT

abstract class StreamingsRepository {

  protected fun processItems(
    remoteItems: List<StreamingService>,
    countryCode: String,
  ) = remoteItems
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

  protected fun processItems(
    remoteItems: TmdbStreamingCountry,
    mediaName: String,
    countryCode: String,
  ): List<StreamingService> {
    val items = mutableListOf<StreamingService>()
    items.addAll(remoteItems.flatrate?.map { createStreamingService(mediaName, countryCode, remoteItems, it, FLATRATE) } ?: emptyList())
    items.addAll(remoteItems.free?.map { createStreamingService(mediaName, countryCode, remoteItems, it, FREE) } ?: emptyList())
    items.addAll(remoteItems.buy?.map { createStreamingService(mediaName, countryCode, remoteItems, it, BUY) } ?: emptyList())
    items.addAll(remoteItems.rent?.map { createStreamingService(mediaName, countryCode, remoteItems, it, RENT) } ?: emptyList())
    items.addAll(remoteItems.ads?.map { createStreamingService(mediaName, countryCode, remoteItems, it, ADS) } ?: emptyList())
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

  private fun createStreamingService(
    mediaName: String,
    countryCode: String,
    country: TmdbStreamingCountry,
    service: TmdbStreamingService,
    option: StreamingService.Option,
  ) = StreamingService(
    imagePath = service.logo_path,
    name = service.provider_name,
    options = listOf(option),
    link = country.link,
    mediaName = mediaName,
    countryCode = countryCode
  )
}
