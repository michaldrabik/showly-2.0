package com.michaldrabik.ui_widgets.calendar_movies

import android.appwidget.AppWidgetManager
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
import com.michaldrabik.common.Mode
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.replace
import com.michaldrabik.ui_model.CalendarMode
import com.michaldrabik.ui_model.ImageStatus
import com.michaldrabik.ui_progress_movies.calendar.cases.items.CalendarMoviesFutureCase
import com.michaldrabik.ui_progress_movies.calendar.cases.items.CalendarMoviesRecentsCase
import com.michaldrabik.ui_progress_movies.calendar.recycler.CalendarMovieListItem
import com.michaldrabik.ui_widgets.BaseWidgetProvider
import com.michaldrabik.ui_widgets.BaseWidgetProvider.Companion.EXTRA_MOVIE_ID
import com.michaldrabik.ui_widgets.R
import kotlinx.coroutines.runBlocking

class CalendarMoviesWidgetViewsFactory(
  private val widgetId: Int,
  private val context: Context,
  private val futureItemsCase: CalendarMoviesFutureCase,
  private val recentItemsCase: CalendarMoviesRecentsCase,
  private val settingsRepository: SettingsRepository,
) : RemoteViewsService.RemoteViewsFactory {

  private val imageCorner by lazy { context.dimenToPx(R.dimen.mediaTileCorner) }
  private val imageWidth by lazy { context.dimenToPx(R.dimen.widgetImageWidth) }
  private val imageHeight by lazy { context.dimenToPx(R.dimen.widgetImageHeight) }
  private var mode = CalendarMode.PRESENT_FUTURE

  private val adapterItems = mutableListOf<CalendarMovieListItem>()

  override fun onDataSetChanged() {
    runBlocking {
      mode = settingsRepository.widgets.getWidgetCalendarMode(Mode.MOVIES, widgetId)
      val items = when (mode) {
        CalendarMode.PRESENT_FUTURE -> futureItemsCase.loadItems()
        CalendarMode.RECENTS -> recentItemsCase.loadItems()
      }
      adapterItems.replace(items)
    }
  }

  override fun getViewAt(position: Int) =
    when (val item = adapterItems[position]) {
      is CalendarMovieListItem.MovieItem -> createItemRemoteView(item)
      is CalendarMovieListItem.Header -> createHeaderRemoteView(item, showIcon = position == 0)
    }

  private fun createHeaderRemoteView(item: CalendarMovieListItem.Header, showIcon: Boolean) =
    RemoteViews(context.packageName, getHeaderLayout()).apply {
      setTextViewText(R.id.progressWidgetHeaderTitle, context.getString(item.textResId))
      setViewVisibility(R.id.progressWidgetHeaderTitleIcon, if (mode == CalendarMode.RECENTS) VISIBLE else GONE)

      if (showIcon) {
        when (mode) {
          CalendarMode.PRESENT_FUTURE -> setImageViewResource(R.id.progressWidgetHeaderIcon, R.drawable.ic_history)
          CalendarMode.RECENTS -> setImageViewResource(R.id.progressWidgetHeaderIcon, R.drawable.ic_calendar)
        }
        setViewVisibility(R.id.progressWidgetHeaderIcon, VISIBLE)
        val fillIntent = Intent().apply {
          putExtras(bundleOf(BaseWidgetProvider.EXTRA_MODE_CLICK to true))
          putExtras(bundleOf(AppWidgetManager.EXTRA_APPWIDGET_ID to widgetId))
        }
        setOnClickFillInIntent(R.id.progressWidgetHeaderIcon, fillIntent)
      } else {
        setViewVisibility(R.id.progressWidgetHeaderIcon, GONE)
      }
    }

  private fun createItemRemoteView(item: CalendarMovieListItem.MovieItem): RemoteViews {
    val translatedTitle = item.translation?.title
    val title =
      if (translatedTitle?.isBlank() == false) translatedTitle
      else item.movie.title

    val translatedDescription = item.translation?.overview
    val overview =
      if (translatedDescription?.isBlank() == false) translatedDescription
      else item.movie.overview

    val date = if (item.movie.released != null) {
      item.dateFormat?.format(item.movie.released)?.capitalizeWords()
    } else {
      context.getString(R.string.textTba)
    }

    val remoteView = RemoteViews(context.packageName, getItemLayout()).apply {
      setTextViewText(R.id.calendarMoviesWidgetItemTitle, title)
      setTextViewText(R.id.calendarMoviesWidgetItemOverview, overview)
      setViewVisibility(R.id.calendarMoviesWidgetItemOverview, if (overview.isBlank()) GONE else VISIBLE)
      setTextViewText(R.id.calendarMoviesWidgetItemDate, date)

      val fillIntent = Intent().apply {
        putExtras(bundleOf(EXTRA_MOVIE_ID to item.movie.traktId))
      }
      setOnClickFillInIntent(R.id.calendarMoviesWidgetItem, fillIntent)
    }

    if (item.image.status != ImageStatus.AVAILABLE) {
      remoteView.setViewVisibility(R.id.calendarMoviesWidgetItemImage, GONE)
      remoteView.setViewVisibility(R.id.calendarMoviesWidgetItemPlaceholder, VISIBLE)
      return remoteView
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

  private fun getItemLayout(): Int {
    val isLight = settingsRepository.widgets.widgetsTheme == MODE_NIGHT_NO
    return when {
      isLight -> R.layout.widget_movies_calendar_item_day
      else -> R.layout.widget_movies_calendar_item_night
    }
  }

  private fun getHeaderLayout(): Int {
    val isLight = settingsRepository.widgets.widgetsTheme == MODE_NIGHT_NO
    return when {
      isLight -> R.layout.widget_header_day
      else -> R.layout.widget_header_night
    }
  }

  override fun getItemId(position: Int) = adapterItems[position].movie.traktId

  override fun getLoadingView() =
    RemoteViews(context.packageName, R.layout.widget_loading_item)

  override fun getCount() = adapterItems.size

  override fun hasStableIds() = true

  override fun getViewTypeCount() = 4

  override fun onCreate() = Unit

  override fun onDestroy() = Unit
}
