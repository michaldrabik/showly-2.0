package com.michaldrabik.ui_widgets.progress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.DurationPrinter
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.replace
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_progress.ProgressItem
import com.michaldrabik.ui_progress.main.cases.ProgressLoadItemsCase
import com.michaldrabik.ui_progress.main.cases.ProgressSortOrderCase
import com.michaldrabik.ui_repository.shows.ShowsRepository
import com.michaldrabik.ui_widgets.R
import com.michaldrabik.ui_widgets.progress.ProgressWidgetProvider.Companion.EXTRA_EPISODE_ID
import com.michaldrabik.ui_widgets.progress.ProgressWidgetProvider.Companion.EXTRA_SEASON_ID
import com.michaldrabik.ui_widgets.progress.ProgressWidgetProvider.Companion.EXTRA_SHOW_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking

class ProgressWidgetViewsFactory(
  private val context: Context,
  private val loadItemsCase: ProgressLoadItemsCase,
  private val sortOrderCase: ProgressSortOrderCase,
  private val showsRepository: ShowsRepository,
  private val imagesProvider: ShowImagesProvider
) : RemoteViewsService.RemoteViewsFactory, CoroutineScope {

  override val coroutineContext = Job() + Dispatchers.Main

  private val imageCorner by lazy { context.dimenToPx(R.dimen.showTileCorner) }
  private val imageWidth by lazy { context.dimenToPx(R.dimen.progressWidgetImageWidth) }
  private val imageHeight by lazy { context.dimenToPx(R.dimen.progressWidgetImageHeight) }
  private val adapterItems by lazy { mutableListOf<ProgressItem>() }
  private val durationPrinter by lazy { DurationPrinter(context.applicationContext) }

  private fun loadData() {
    runBlocking {
      val shows = showsRepository.myShows.loadAll()
      val items = shows.map { show ->
        async {
          val item = loadItemsCase.loadProgressItem(show)
          val image = imagesProvider.findCachedImage(show, ImageType.POSTER)
          item.copy(image = image)
        }
      }.awaitAll()

      val sortOrder = sortOrderCase.loadSortOrder()
      val allItems = loadItemsCase.prepareWatchlistItems(items, "", sortOrder).toMutableList()

      val headerIndex = allItems.indexOfFirst { !it.isHeader() && !it.episode.hasAired(it.season) }
      if (headerIndex != -1) {
        val item = allItems[headerIndex]
        allItems.add(headerIndex, item.copy(headerTextResId = R.string.textWatchlistIncoming))
      }

      adapterItems.replace(allItems)
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

  private fun createItemRemoteView(item: ProgressItem): RemoteViews {
    val subtitle = String.format("S.%02d E.%02d", item.episode.season, item.episode.number)
    val progressText = "${item.watchedEpisodesCount}/${item.episodesCount}"
    val imageUrl = item.image.fullFileUrl
    val hasAired = item.episode.hasAired(item.season)
    val subtitle2 = when {
      item.episode.title.isBlank() -> context.getString(R.string.textTba)
      item.translation?.title?.isBlank() == false -> item.translation?.title ?: context.getString(R.string.textTba)
      else -> item.episode.title
    }

    val remoteView = RemoteViews(context.packageName, R.layout.widget_progress_item).apply {
      setTextViewText(R.id.progressWidgetItemTitle, item.show.title)
      setTextViewText(R.id.progressWidgetItemSubtitle, subtitle)
      setTextViewText(R.id.progressWidgetItemSubtitle2, subtitle2)
      setTextViewText(R.id.progressWidgetItemProgressText, progressText)
      setViewVisibility(R.id.progressWidgetItemBadge, if (item.isNew()) VISIBLE else GONE)
      setProgressBar(R.id.progressWidgetItemProgress, item.episodesCount, item.watchedEpisodesCount, false)
      if (hasAired) {
        setViewVisibility(R.id.progressWidgetItemCheckButton, VISIBLE)
        setViewVisibility(R.id.progressWidgetItemDateButton, GONE)
      } else {
        setViewVisibility(R.id.progressWidgetItemCheckButton, GONE)
        setViewVisibility(R.id.progressWidgetItemDateButton, VISIBLE)
        setTextViewText(R.id.progressWidgetItemDateButton, durationPrinter.print(item.episode.firstAired))
      }

      val fillIntent = Intent().apply {
        putExtras(
          Bundle().apply {
            putExtra(EXTRA_SHOW_ID, item.show.ids.trakt.id)
          }
        )
      }
      setOnClickFillInIntent(R.id.progressWidgetItem, fillIntent)

      val checkFillIntent = Intent().apply {
        putExtras(
          Bundle().apply {
            putExtra(EXTRA_EPISODE_ID, item.episode.ids.trakt.id)
            putExtra(EXTRA_SEASON_ID, item.season.ids.trakt.id)
            putExtra(EXTRA_SHOW_ID, item.show.ids.trakt.id)
          }
        )
      }
      setOnClickFillInIntent(R.id.progressWidgetItemCheckButton, checkFillIntent)
    }

    try {
      val bitmap = Glide.with(context).asBitmap()
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

  private fun createHeaderRemoteView(item: ProgressItem) =
    RemoteViews(context.packageName, R.layout.widget_progress_header).apply {
      setTextViewText(R.id.progressWidgetHeaderTitle, context.getString(item.headerTextResId!!))
    }

  override fun getItemId(position: Int) = adapterItems[position].show.ids.trakt.id

  override fun onDataSetChanged() = loadData()

  override fun getLoadingView() =
    RemoteViews(context.packageName, R.layout.widget_progress_loading)

  override fun getCount() = adapterItems.size

  override fun hasStableIds() = true

  override fun getViewTypeCount() = 2

  override fun onDestroy() {
    coroutineContext.cancelChildren()
    adapterItems.clear()
  }
}
