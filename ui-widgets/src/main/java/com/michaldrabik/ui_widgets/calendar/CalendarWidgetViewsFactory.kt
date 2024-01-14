package com.michaldrabik.ui_widgets.calendar

import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
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
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.replace
import com.michaldrabik.ui_model.CalendarMode.PRESENT_FUTURE
import com.michaldrabik.ui_model.CalendarMode.RECENTS
import com.michaldrabik.ui_model.ImageStatus
import com.michaldrabik.ui_progress.calendar.cases.items.CalendarFutureCase
import com.michaldrabik.ui_progress.calendar.cases.items.CalendarRecentsCase
import com.michaldrabik.ui_progress.calendar.recycler.CalendarListItem
import com.michaldrabik.ui_widgets.BaseWidgetProvider.Companion.EXTRA_MODE_CLICK
import com.michaldrabik.ui_widgets.BaseWidgetProvider.Companion.EXTRA_SHOW_ID
import com.michaldrabik.ui_widgets.R
import kotlinx.coroutines.runBlocking
import java.util.Locale

class CalendarWidgetViewsFactory(
  private val widgetId: Int,
  private val context: Context,
  private val calendarFutureCase: CalendarFutureCase,
  private val calendarRecentsCase: CalendarRecentsCase,
  private val settingsRepository: SettingsRepository,
) : RemoteViewsService.RemoteViewsFactory {

  private val imageCorner by lazy { context.dimenToPx(R.dimen.mediaTileCorner) }
  private val imageWidth by lazy { context.dimenToPx(R.dimen.widgetImageWidth) }
  private val imageHeight by lazy { context.dimenToPx(R.dimen.widgetImageHeight) }
  private var mode = PRESENT_FUTURE

  private val adapterItems = mutableListOf<CalendarListItem>()

  override fun onDataSetChanged() {
    runBlocking {
      mode = settingsRepository.widgets.getWidgetCalendarMode(Mode.SHOWS, widgetId)
      val items = when (mode) {
        PRESENT_FUTURE -> calendarFutureCase.loadItems()
        RECENTS -> calendarRecentsCase.loadItems()
      }
      adapterItems.replace(items)
    }
  }

  override fun getViewAt(position: Int) =
    when (val item = adapterItems[position]) {
      is CalendarListItem.Episode -> createItemRemoteView(item)
      is CalendarListItem.Header -> createHeaderRemoteView(item, showIcon = position == 0)
    }

  private fun createHeaderRemoteView(item: CalendarListItem.Header, showIcon: Boolean) =
    RemoteViews(context.packageName, getHeaderLayout()).apply {
      setTextViewText(R.id.progressWidgetHeaderTitle, context.getString(item.textResId))
      setViewVisibility(R.id.progressWidgetHeaderTitleIcon, if (mode == RECENTS) VISIBLE else GONE)

      if (showIcon) {
        when (mode) {
          PRESENT_FUTURE -> setImageViewResource(R.id.progressWidgetHeaderIcon, R.drawable.ic_history)
          RECENTS -> setImageViewResource(R.id.progressWidgetHeaderIcon, R.drawable.ic_calendar)
        }
        setViewVisibility(R.id.progressWidgetHeaderIcon, VISIBLE)
        val fillIntent = Intent().apply {
          putExtras(bundleOf(EXTRA_MODE_CLICK to true))
          putExtras(bundleOf(EXTRA_APPWIDGET_ID to widgetId))
        }
        setOnClickFillInIntent(R.id.progressWidgetHeaderIcon, fillIntent)
      } else {
        setViewVisibility(R.id.progressWidgetHeaderIcon, GONE)
      }
    }

  private fun createItemRemoteView(item: CalendarListItem.Episode): RemoteViews {

    val remoteView = RemoteViews(context.packageName, getItemLayout()).apply {
      val translatedTitle = item.translations?.show?.title
      val title =
        if (translatedTitle?.isBlank() == false) translatedTitle
        else item.show.title
      setTextViewText(R.id.calendarWidgetItemTitle, title)

      val date = item.episode.firstAired?.toLocalZone()?.let { item.dateFormat?.format(it)?.capitalizeWords() }
      setTextViewText(R.id.calendarWidgetItemDate, date)

      val isNewSeason = item.episode.number == 1
      if (isNewSeason) {
        setTextViewText(R.id.calendarWidgetItemOverview, String.format(Locale.ENGLISH, context.getString(R.string.textSeason), item.episode.season))
        setTextViewText(R.id.calendarWidgetItemBadge, context.getString(R.string.textNewSeason))
      } else {
        val episodeTitle = when {
          item.episode.title.isBlank() -> context.getString(R.string.textTba)
          item.translations?.episode?.title?.isBlank() == false -> item.translations?.episode?.title
          item.episode.title == "Episode ${item.episode.number}" -> String.format(
            Locale.ENGLISH,
            context.getString(R.string.textEpisode),
            item.episode.number
          )
          else -> item.episode.title
        }
        val badgeTitle = String.format(
          Locale.ENGLISH,
          context.getString(com.michaldrabik.ui_progress.R.string.textSeasonEpisode),
          item.episode.season,
          item.episode.number
        ).plus(
          item.episode.numberAbs?.let { if (it > 0 && item.show.isAnime) " ($it)" else "" } ?: ""
        )

        setTextViewText(R.id.calendarWidgetItemOverview, episodeTitle)
        setTextViewText(R.id.calendarWidgetItemBadge, badgeTitle)
      }

      val fillIntent = Intent().apply {
        putExtras(bundleOf(EXTRA_SHOW_ID to item.show.traktId))
      }
      setOnClickFillInIntent(R.id.calendarWidgetItem, fillIntent)

      setViewVisibility(R.id.calendarWidgetItemImageBadge, if (item.isWatchlist) VISIBLE else GONE)
    }

    if (item.image.status != ImageStatus.AVAILABLE) {
      remoteView.setViewVisibility(R.id.calendarWidgetItemImage, GONE)
      remoteView.setViewVisibility(R.id.calendarWidgetItemPlaceholder, VISIBLE)
      return remoteView
    }

    try {
      remoteView.setViewVisibility(R.id.calendarWidgetItemImage, GONE)
      remoteView.setViewVisibility(R.id.calendarWidgetItemPlaceholder, GONE)

      val bitmap = Glide.with(context)
        .asBitmap()
        .load(item.image.fullFileUrl)
        .transform(CenterCrop(), RoundedCorners(imageCorner))
        .submit(imageWidth, imageHeight)
        .get()

      remoteView.setImageViewBitmap(R.id.calendarWidgetItemImage, bitmap)
      remoteView.setViewVisibility(R.id.calendarWidgetItemImage, VISIBLE)
    } catch (t: Throwable) {
      remoteView.setViewVisibility(R.id.calendarWidgetItemImage, GONE)
      remoteView.setViewVisibility(R.id.calendarWidgetItemPlaceholder, VISIBLE)
    }

    return remoteView
  }

  private fun getItemLayout(): Int {
    val isLight = settingsRepository.widgets.widgetsTheme == MODE_NIGHT_NO
    return when {
      isLight -> R.layout.widget_calendar_item_day
      else -> R.layout.widget_calendar_item_night
    }
  }

  private fun getHeaderLayout(): Int {
    val isLight = settingsRepository.widgets.widgetsTheme == MODE_NIGHT_NO
    return when {
      isLight -> R.layout.widget_header_day
      else -> R.layout.widget_header_night
    }
  }

  override fun getItemId(position: Int) = adapterItems[position].show.traktId

  override fun getLoadingView() = RemoteViews(context.packageName, R.layout.widget_loading_item)

  override fun getCount() = adapterItems.size

  override fun hasStableIds() = true

  override fun getViewTypeCount() = 4

  override fun onCreate() = Unit

  override fun onDestroy() = Unit
}
