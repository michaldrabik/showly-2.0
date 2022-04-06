package com.michaldrabik.ui_progress_movies.calendar.cases.items

import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_progress_movies.calendar.helpers.filters.CalendarRecentsFilter
import com.michaldrabik.ui_progress_movies.calendar.helpers.groupers.CalendarRecentsGrouper
import com.michaldrabik.ui_progress_movies.calendar.helpers.sorter.CalendarRecentsSorter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarMoviesRecentsCase @Inject constructor(
  moviesRepository: MoviesRepository,
  translationsRepository: TranslationsRepository,
  imagesProvider: MovieImagesProvider,
  dateFormatProvider: DateFormatProvider,
  override val filter: CalendarRecentsFilter,
  override val grouper: CalendarRecentsGrouper,
  override val sorter: CalendarRecentsSorter,
) : CalendarMoviesItemsCase(
  moviesRepository,
  translationsRepository,
  imagesProvider,
  dateFormatProvider
)
