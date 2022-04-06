package com.michaldrabik.ui_progress_movies.calendar.cases.items

import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_progress_movies.calendar.helpers.filters.CalendarFutureFilter
import com.michaldrabik.ui_progress_movies.calendar.helpers.groupers.CalendarFutureGrouper
import com.michaldrabik.ui_progress_movies.calendar.helpers.sorter.CalendarFutureSorter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarMoviesFutureCase @Inject constructor(
  moviesRepository: MoviesRepository,
  translationsRepository: TranslationsRepository,
  imagesProvider: MovieImagesProvider,
  dateFormatProvider: DateFormatProvider,
  override val filter: CalendarFutureFilter,
  override val grouper: CalendarFutureGrouper,
  override val sorter: CalendarFutureSorter,
) : CalendarMoviesItemsCase(
  moviesRepository,
  translationsRepository,
  imagesProvider,
  dateFormatProvider
)
