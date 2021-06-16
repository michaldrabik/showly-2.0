package com.michaldrabik.ui_progress.calendar.cases.items

import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.Episode
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_progress.calendar.helpers.filters.CalendarFutureFilter
import com.michaldrabik.ui_progress.calendar.helpers.groupers.CalendarFutureGrouper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarFutureCase @Inject constructor(
  database: AppDatabase,
  mappers: Mappers,
  showsRepository: ShowsRepository,
  translationsRepository: TranslationsRepository,
  imagesProvider: ShowImagesProvider,
  dateFormatProvider: DateFormatProvider,
  override val filter: CalendarFutureFilter,
  override val grouper: CalendarFutureGrouper,
) : CalendarItemsCase(
  database,
  mappers,
  showsRepository,
  translationsRepository,
  imagesProvider,
  dateFormatProvider
) {

  override fun sortEpisodes() =
    compareBy<Episode> { it.firstAired }
      .thenByDescending { it.idShowTrakt }
      .thenBy { it.episodeNumber }

  override fun isWatched(episode: Episode) = true
}
