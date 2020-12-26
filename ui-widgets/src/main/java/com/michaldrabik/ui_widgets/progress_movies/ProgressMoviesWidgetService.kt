package com.michaldrabik.ui_widgets.progress_movies

import android.content.Intent
import android.widget.RemoteViewsService
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_progress_movies.main.cases.ProgressMoviesLoadItemsCase
import com.michaldrabik.ui_progress_movies.main.cases.ProgressMoviesSortOrderCase
import com.michaldrabik.ui_repository.movies.MoviesRepository
import com.michaldrabik.ui_widgets.di.UiWidgetsComponentProvider
import javax.inject.Inject

class ProgressMoviesWidgetService : RemoteViewsService() {

  @Inject lateinit var progressLoadItemsCase: ProgressMoviesLoadItemsCase
  @Inject lateinit var progressSortOrderCase: ProgressMoviesSortOrderCase
  @Inject lateinit var moviesRepository: MoviesRepository
  @Inject lateinit var imagesProvider: MovieImagesProvider

  override fun onCreate() {
    super.onCreate()
    (application as UiWidgetsComponentProvider).provideWidgetsComponent().inject(this)
  }

  override fun onGetViewFactory(intent: Intent?) =
    ProgressMoviesWidgetViewsFactory(
      applicationContext,
      progressLoadItemsCase,
      progressSortOrderCase,
      imagesProvider
    )
}
