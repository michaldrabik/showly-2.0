package com.michaldrabik.ui_widgets.calendar_movies

import android.content.Context
import android.content.Intent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.michaldrabik.common.extensions.toFullDayDisplayString
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.replace
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_progress_movies.ProgressMovieItem
import com.michaldrabik.ui_progress_movies.calendar.cases.ProgressMoviesCalendarCase
import com.michaldrabik.ui_progress_movies.main.cases.ProgressMoviesLoadItemsCase
import com.michaldrabik.ui_widgets.R
import com.michaldrabik.ui_widgets.progress_movies.ProgressMoviesWidgetProvider.Companion.EXTRA_MOVIE_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking

class CalendarMoviesWidgetViewsFactory(
  private val context: Context,
  private val loadItemsCase: ProgressMoviesLoadItemsCase,
  private val calendarCase: ProgressMoviesCalendarCase,
  private val imagesProvider: MovieImagesProvider
) : RemoteViewsService.RemoteViewsFactory, CoroutineScope {

  override val coroutineContext = Job() + Dispatchers.Main

  private val imageCorner by lazy { context.dimenToPx(R.dimen.showTileCorner) }
  private val imageWidth by lazy { context.dimenToPx(R.dimen.progressWidgetImageWidth) }
  private val imageHeight by lazy { context.dimenToPx(R.dimen.progressWidgetImageHeight) }
  private val adapterItems by lazy { mutableListOf<ProgressMovieItem>() }

  private fun loadData() {
    runBlocking {
      val movies = loadItemsCase.loadWatchlistMovies()
      val items = movies.map { movie ->
        async {
          val item = loadItemsCase.loadProgressItem(movie)
          val image = imagesProvider.findCachedImage(movie, ImageType.POSTER)
          item.copy(image = image)
        }
      }.awaitAll()

      val groupedItems = calendarCase.prepareItems(items)
      adapterItems.replace(groupedItems)
    }
  }

  override fun onCreate() = loadData()

  override fun getViewAt(position: Int): RemoteViews {
    val item = adapterItems[position]
    return when {
      item.isHeader() -> createHeaderRemoteView(item)
      else -> createItemRemoteView(item)
    }
  }

  private fun createHeaderRemoteView(item: ProgressMovieItem) =
    RemoteViews(context.packageName, R.layout.widget_header).apply {
      setTextViewText(R.id.progressWidgetHeaderTitle, context.getString(item.headerTextResId!!))
    }

  private fun createItemRemoteView(item: ProgressMovieItem): RemoteViews {
    val translatedTitle = item.movieTranslation?.title
    val title =
      if (translatedTitle?.isBlank() == false) translatedTitle.capitalizeWords()
      else item.movie.title

    val translatedDescription = item.movieTranslation?.overview
    val overview =
      if (translatedDescription?.isBlank() == false) translatedDescription.capitalizeWords()
      else item.movie.overview

    val date = item.movie.released?.toFullDayDisplayString()
      ?: context.getString(com.michaldrabik.ui_progress_movies.R.string.textTba)

    val remoteView = RemoteViews(context.packageName, R.layout.widget_movies_calendar_item).apply {
      setTextViewText(R.id.calendarMoviesWidgetItemTitle, title)
      setTextViewText(R.id.calendarMoviesWidgetItemOverview, overview)
      setTextViewText(R.id.calendarMoviesWidgetItemDate, date)

      val fillIntent = Intent().apply {
        putExtras(bundleOf(EXTRA_MOVIE_ID to item.movie.traktId))
      }
      setOnClickFillInIntent(R.id.calendarMoviesWidgetItem, fillIntent)
    }

    try {
      remoteView.setViewVisibility(R.id.calendarMoviesWidgetItemImage, GONE)
      remoteView.setViewVisibility(R.id.calendarMoviesWidgetItemPlaceholder, GONE)

      val bitmap = Glide.with(context)
        .asBitmap()
        .load(item.image.fullFileUrl)
        .transform(CenterCrop(), RoundedCorners(imageCorner))
        .submit(imageWidth, imageHeight)
        .get()

      remoteView.setImageViewBitmap(R.id.calendarMoviesWidgetItemImage, bitmap)
      remoteView.setViewVisibility(R.id.calendarMoviesWidgetItemImage, VISIBLE)
    } catch (t: Throwable) {
      remoteView.setViewVisibility(R.id.calendarMoviesWidgetItemImage, GONE)
      remoteView.setViewVisibility(R.id.calendarMoviesWidgetItemPlaceholder, VISIBLE)
    }

    return remoteView
  }

  override fun getItemId(position: Int) = adapterItems[position].movie.traktId

  override fun onDataSetChanged() = loadData()

  override fun getLoadingView() =
    RemoteViews(context.packageName, R.layout.widget_progress_loading)

  override fun getCount() = adapterItems.size

  override fun hasStableIds() = true

  override fun getViewTypeCount() = 2

  override fun onDestroy() = coroutineContext.cancelChildren()
}
