package com.michaldrabik.ui_widgets.progress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.utilities.DurationPrinter
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.replace
import com.michaldrabik.ui_model.ImageStatus
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_progress.progress.cases.ProgressItemsCase
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem
import com.michaldrabik.ui_widgets.BaseWidgetProvider.Companion.EXTRA_SHOW_ID
import com.michaldrabik.ui_widgets.R
import com.michaldrabik.ui_widgets.progress.ProgressWidgetProvider.Companion.EXTRA_EPISODE_ID
import com.michaldrabik.ui_widgets.progress.ProgressWidgetProvider.Companion.EXTRA_SEASON_ID
import kotlinx.coroutines.runBlocking
import java.util.Locale.ENGLISH
import kotlin.math.roundToInt

class ProgressWidgetViewsFactory(
  private val context: Context,
  private val itemsCase: ProgressItemsCase,
  private val settingsRepository: SettingsRepository,
) : RemoteViewsService.RemoteViewsFactory {

  private val imageCorner by lazy { context.dimenToPx(R.dimen.mediaTileCorner) }
  private val imageWidth by lazy { context.dimenToPx(R.dimen.widgetImageWidth) }
  private val imageHeight by lazy { context.dimenToPx(R.dimen.widgetImageHeight) }
  private val checkWidth by lazy { context.dimenToPx(R.dimen.widgetCheckButtonWidth) }
  private val spaceMedium by lazy { context.dimenToPx(R.dimen.spaceMedium) }
  private val adapterItems by lazy { mutableListOf<ProgressListItem>() }
  private val durationPrinter by lazy { DurationPrinter(context.applicationContext) }

  override fun onDataSetChanged() {
    runBlocking {
      val items = itemsCase.loadItems("")
        .filterNot { it is ProgressListItem.Filters }
      adapterItems.replace(items)
    }
  }

  override fun getViewAt(position: Int) =
    when (val item = adapterItems[position]) {
      is ProgressListItem.Episode -> createItemRemoteView(item)
      is ProgressListItem.Header -> createHeaderRemoteView(item)
      else -> throw IllegalStateException()
    }

  private fun createItemRemoteView(item: ProgressListItem.Episode): RemoteViews {
    val title =
      if (item.translations?.show?.title?.isBlank() == false) item.translations?.show?.title
      else item.show.title

    val subtitle = String.format(ENGLISH, "S.%02d E.%02d", item.episode?.season, item.episode?.number)
      .plus(item.episode?.numberAbs?.let { if (it > 0 && item.show.isAnime) " ($it)" else "" } ?: "")

    var percent = 0
    if (item.totalCount != 0) {
      percent = ((item.watchedCount.toFloat() / item.totalCount.toFloat()) * 100F).roundToInt()
    }
    val progressText =
      String.format(ENGLISH, "%d/%d (%d%%)", item.watchedCount, item.totalCount, percent)
    val imageUrl = item.image.fullFileUrl
    val hasAired = item.episode?.hasAired(item.season ?: Season.EMPTY) == true
    val subtitle2 = when {
      item.episode?.title?.isBlank() == true -> context.getString(R.string.textTba)
      item.translations?.episode?.title?.isBlank() == false ->
        item.translations?.episode?.title ?: context.getString(R.string.textTba)
      item.episode?.title == "Episode ${item.episode?.number}" ->
        String.format(ENGLISH, context.getString(R.string.textEpisode), item.episode?.number)
      else -> item.episode?.title
    }

    val remoteView = RemoteViews(context.packageName, getItemLayout()).apply {
      setTextViewText(R.id.progressWidgetItemTitle, title)
      setTextViewText(R.id.progressWidgetItemSubtitle, subtitle)
      setTextViewText(R.id.progressWidgetItemSubtitle2, subtitle2)
      setTextViewText(R.id.progressWidgetItemProgressText, progressText)
      setViewVisibility(R.id.progressWidgetItemBadge, if (item.isNew()) VISIBLE else GONE)
      setProgressBar(R.id.progressWidgetItemProgress, item.totalCount, item.watchedCount, false)
      if (hasAired) {
        setViewVisibility(R.id.progressWidgetItemCheckButton, VISIBLE)
        setViewVisibility(R.id.progressWidgetItemDateButton, GONE)
        setViewPadding(R.id.progressWidgetItemProgress, 0, 0, checkWidth, 0)
      } else {
        setViewVisibility(R.id.progressWidgetItemCheckButton, GONE)
        setViewVisibility(R.id.progressWidgetItemDateButton, VISIBLE)
        setTextViewText(R.id.progressWidgetItemDateButton, durationPrinter.print(item.episode?.firstAired))
        setViewPadding(R.id.progressWidgetItemProgress, 0, 0, spaceMedium, 0)
      }

      val fillIntent = Intent().apply {
        putExtras(
          Bundle().apply {
            putExtra(EXTRA_SHOW_ID, item.show.traktId)
          }
        )
      }
      setOnClickFillInIntent(R.id.progressWidgetItem, fillIntent)

      val checkFillIntent = Intent().apply {
        putExtras(
          Bundle().apply {
            putExtra(EXTRA_EPISODE_ID, item.episode?.ids?.trakt?.id)
            putExtra(EXTRA_SEASON_ID, item.season?.ids?.trakt?.id)
            putExtra(EXTRA_SHOW_ID, item.show.traktId)
          }
        )
      }
      setOnClickFillInIntent(R.id.progressWidgetItemCheckButton, checkFillIntent)
    }

    if (item.image.status != ImageStatus.AVAILABLE) {
      remoteView.setViewVisibility(R.id.progressWidgetItemImage, GONE)
      remoteView.setViewVisibility(R.id.progressWidgetItemPlaceholder, VISIBLE)
      return remoteView
    }

    try {
      remoteView.setViewVisibility(R.id.progressWidgetItemImage, GONE)
      remoteView.setViewVisibility(R.id.progressWidgetItemPlaceholder, GONE)

      val bitmap = Glide.with(context)
        .asBitmap()
        .load(imageUrl)
        .transform(CenterCrop(), RoundedCorners(imageCorner))
        .submit(imageWidth, imageHeight)
        .get()

      remoteView.setImageViewBitmap(R.id.progressWidgetItemImage, bitmap)
      remoteView.setViewVisibility(R.id.progressWidgetItemImage, VISIBLE)
    } catch (t: Throwable) {
      remoteView.setViewVisibility(R.id.progressWidgetItemImage, GONE)
      remoteView.setViewVisibility(R.id.progressWidgetItemPlaceholder, VISIBLE)
    }

    return remoteView
  }

  private fun createHeaderRemoteView(item: ProgressListItem.Header) =
    RemoteViews(context.packageName, getHeaderLayout()).apply {
      setTextViewText(R.id.progressWidgetHeaderTitle, context.getString(item.textResId))
    }

  private fun getItemLayout(): Int {
    val isLight = settingsRepository.widgets.widgetsTheme == MODE_NIGHT_NO
    return when {
      isLight -> R.layout.widget_progress_item_day
      else -> R.layout.widget_progress_item_night
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
