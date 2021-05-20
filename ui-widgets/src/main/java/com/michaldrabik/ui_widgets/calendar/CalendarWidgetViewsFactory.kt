package com.michaldrabik.ui_widgets.calendar

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
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.replace
import com.michaldrabik.ui_model.ImageStatus
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_progress.ProgressItem
import com.michaldrabik.ui_progress.calendar.cases.ProgressCalendarCase
import com.michaldrabik.ui_progress.main.cases.ProgressLoadItemsCase
import com.michaldrabik.ui_widgets.BaseWidgetProvider.Companion.EXTRA_SHOW_ID
import com.michaldrabik.ui_widgets.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking
import java.util.Locale

class CalendarWidgetViewsFactory(
  private val context: Context,
  private val loadItemsCase: ProgressLoadItemsCase,
  private val calendarCase: ProgressCalendarCase,
  private val imagesProvider: ShowImagesProvider,
  private val settingsRepository: SettingsRepository
) : RemoteViewsService.RemoteViewsFactory, CoroutineScope {

  override val coroutineContext = Job() + Dispatchers.Main

  private val imageCorner by lazy { context.dimenToPx(R.dimen.showTileCorner) }
  private val imageWidth by lazy { context.dimenToPx(R.dimen.widgetImageWidth) }
  private val imageHeight by lazy { context.dimenToPx(R.dimen.widgetImageHeight) }
  private val adapterItems by lazy { mutableListOf<ProgressItem>() }

  private fun loadData() {
    runBlocking {
      val shows = loadItemsCase.loadMyShows()
      val dateFormat = loadItemsCase.loadDateFormat()
      val items = shows.map { show ->
        async {
          val item = loadItemsCase.loadProgressItem(show)
          try {
            val image = imagesProvider.loadRemoteImage(show, ImageType.POSTER)
            item.copy(image = image, dateFormat = dateFormat)
          } catch (error: Throwable) {
            item
          }
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

  private fun createHeaderRemoteView(item: ProgressItem) =
    RemoteViews(context.packageName, getHeaderLayout()).apply {
      setTextViewText(R.id.progressWidgetHeaderTitle, context.getString(item.headerTextResId!!))
    }

  private fun createItemRemoteView(item: ProgressItem): RemoteViews {
    val translatedTitle = item.showTranslation?.title
    val title =
      if (translatedTitle?.isBlank() == false) translatedTitle
      else item.show.title

    val episodeBadgeText = String.format(
      Locale.ENGLISH,
      context.getString(R.string.textSeasonEpisode),
      item.upcomingEpisode.season,
      item.upcomingEpisode.number
    )

    val episodeTitle = when {
      item.upcomingEpisode.title.isBlank() -> context.getString(R.string.textTba)
      item.upcomingEpisodeTranslation?.title?.isBlank() == false -> item.upcomingEpisodeTranslation?.title
      else -> item.upcomingEpisode.title
    }

    val date = item.upcomingEpisode.firstAired?.toLocalZone()?.let { item.dateFormat?.format(it)?.capitalizeWords() }

    val remoteView = RemoteViews(context.packageName, getItemLayout()).apply {
      setTextViewText(R.id.calendarWidgetItemTitle, title)
      setTextViewText(R.id.calendarWidgetItemOverview, episodeTitle)
      setTextViewText(R.id.calendarWidgetItemBadge, episodeBadgeText)
      setTextViewText(R.id.calendarWidgetItemDate, date)

      val fillIntent = Intent().apply {
        putExtras(bundleOf(EXTRA_SHOW_ID to item.show.traktId))
      }
      setOnClickFillInIntent(R.id.calendarWidgetItem, fillIntent)
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
    val isLight = settingsRepository.widgetsTheme == MODE_NIGHT_NO
    return when {
      isLight -> R.layout.widget_calendar_item_day
      else -> R.layout.widget_calendar_item_night
    }
  }

  private fun getHeaderLayout(): Int {
    val isLight = settingsRepository.widgetsTheme == MODE_NIGHT_NO
    return when {
      isLight -> R.layout.widget_header_day
      else -> R.layout.widget_header_night
    }
  }

  override fun getItemId(position: Int) = adapterItems[position].show.traktId

  override fun onDataSetChanged() = loadData()

  override fun getLoadingView() = RemoteViews(context.packageName, R.layout.widget_loading_item)

  override fun getCount() = adapterItems.size

  override fun hasStableIds() = true

  override fun getViewTypeCount() = 4

  override fun onDestroy() = coroutineContext.cancelChildren()
}
