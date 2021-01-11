package com.michaldrabik.ui_widgets.progress_movies

import android.content.Context
import android.content.Intent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.replace
import com.michaldrabik.ui_model.ImageStatus
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_progress_movies.ProgressMovieItem
import com.michaldrabik.ui_progress_movies.main.cases.ProgressMoviesLoadItemsCase
import com.michaldrabik.ui_progress_movies.main.cases.ProgressMoviesSortOrderCase
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_widgets.BaseWidgetProvider.Companion.EXTRA_MOVIE_ID
import com.michaldrabik.ui_widgets.R
import com.michaldrabik.ui_widgets.progress_movies.ProgressMoviesWidgetProvider.Companion.EXTRA_CHECK_MOVIE_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking

class ProgressMoviesWidgetViewsFactory(
  private val context: Context,
  private val loadItemsCase: ProgressMoviesLoadItemsCase,
  private val sortOrderCase: ProgressMoviesSortOrderCase,
  private val imagesProvider: MovieImagesProvider,
  private val settingsRepository: SettingsRepository
) : RemoteViewsService.RemoteViewsFactory, CoroutineScope {

  override val coroutineContext = Job() + Dispatchers.Main

  private val imageCorner by lazy { context.dimenToPx(R.dimen.showTileCorner) }
  private val imageWidth by lazy { context.dimenToPx(R.dimen.widgetImageWidth) }
  private val imageHeight by lazy { context.dimenToPx(R.dimen.widgetImageHeight) }
  private val adapterItems by lazy { mutableListOf<ProgressMovieItem>() }

  private fun loadData() {
    runBlocking {
      val movies = loadItemsCase.loadWatchlistMovies()
        .filter { it.released == null || it.hasAired() }
      val items = movies.map { m ->
        async {
          val item = loadItemsCase.loadProgressItem(m)
          try {
            val image = imagesProvider.loadRemoteImage(m, ImageType.POSTER)
            item.copy(image = image)
          } catch (error: Throwable) {
            item
          }
        }
      }.awaitAll()

      val sortOrder = sortOrderCase.loadSortOrder()
      val allItems = loadItemsCase.prepareItems(items, "", sortOrder).toMutableList()

      adapterItems.replace(allItems)
    }
  }

  override fun onCreate() = loadData()

  override fun getViewAt(position: Int) = createItemRemoteView(adapterItems[position])

  private fun createItemRemoteView(item: ProgressMovieItem): RemoteViews {
    val translatedTitle = item.movieTranslation?.title
    val title =
      if (translatedTitle?.isBlank() == false) translatedTitle.capitalizeWords()
      else item.movie.title

    val translatedDescription = item.movieTranslation?.overview
    val description =
      if (translatedDescription?.isBlank() == false) translatedDescription.capitalizeWords()
      else item.movie.overview

    val remoteView = RemoteViews(context.packageName, getItemLayout()).apply {
      setTextViewText(R.id.progressMoviesWidgetItemTitle, title)
      setTextViewText(R.id.progressMoviesWidgetItemSubtitle2, description)

      val fillIntent = Intent().apply {
        putExtras(bundleOf(EXTRA_MOVIE_ID to item.movie.traktId))
      }
      setOnClickFillInIntent(R.id.progressMoviesWidgetItem, fillIntent)

      val checkFillIntent = Intent().apply {
        putExtras(bundleOf(EXTRA_CHECK_MOVIE_ID to item.movie.traktId))
      }
      setOnClickFillInIntent(R.id.progressMoviesWidgetItemCheckButton, checkFillIntent)
    }

    if (item.image.status != ImageStatus.AVAILABLE) {
      remoteView.setViewVisibility(R.id.progressMoviesWidgetItemImage, GONE)
      remoteView.setViewVisibility(R.id.progressMoviesWidgetItemPlaceholder, VISIBLE)
      return remoteView
    }

    try {
      remoteView.setViewVisibility(R.id.progressMoviesWidgetItemImage, GONE)
      remoteView.setViewVisibility(R.id.progressMoviesWidgetItemPlaceholder, GONE)

      val bitmap = Glide.with(context)
        .asBitmap()
        .load(item.image.fullFileUrl)
        .transform(CenterCrop(), RoundedCorners(imageCorner))
        .submit(imageWidth, imageHeight)
        .get()

      remoteView.setImageViewBitmap(R.id.progressMoviesWidgetItemImage, bitmap)
      remoteView.setViewVisibility(R.id.progressMoviesWidgetItemImage, VISIBLE)
    } catch (t: Throwable) {
      remoteView.setViewVisibility(R.id.progressMoviesWidgetItemImage, GONE)
      remoteView.setViewVisibility(R.id.progressMoviesWidgetItemPlaceholder, VISIBLE)
    }

    return remoteView
  }

  private fun getItemLayout(): Int {
    val isLight = settingsRepository.widgetsTheme == MODE_NIGHT_NO
    return when {
      isLight -> R.layout.widget_movies_progress_item_day
      else -> R.layout.widget_movies_progress_item_night
    }
  }

  override fun getItemId(position: Int) = adapterItems[position].movie.traktId

  override fun onDataSetChanged() = loadData()

  override fun getLoadingView() =
    RemoteViews(context.packageName, R.layout.widget_loading_item)

  override fun getCount() = adapterItems.size

  override fun hasStableIds() = true

  override fun getViewTypeCount() = 2

  override fun onDestroy() = coroutineContext.cancelChildren()
}
