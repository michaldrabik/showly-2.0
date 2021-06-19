package com.michaldrabik.ui_progress.calendar.cases.items

import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.Episode
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_progress.calendar.helpers.filters.CalendarRecentsFilter
import com.michaldrabik.ui_progress.calendar.helpers.groupers.CalendarRecentsGrouper
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class CalendarRecentsCase @Inject constructor(
  database: AppDatabase,
  mappers: Mappers,
  showsRepository: ShowsRepository,
  translationsRepository: TranslationsRepository,
  imagesProvider: ShowImagesProvider,
  dateFormatProvider: DateFormatProvider,
  override val filter: CalendarRecentsFilter,
  override val grouper: CalendarRecentsGrouper,
) : CalendarItemsCase(
  database,
  mappers,
  showsRepository,
  translationsRepository,
  imagesProvider,
  dateFormatProvider
) {

  override fun sortEpisodes() =
    compareByDescending<Episode> { it.firstAired }
      .thenByDescending { it.idShowTrakt }
      .thenByDescending { it.episodeNumber }

  override fun isWatched(episode: Episode) = episode.isWatched
}
