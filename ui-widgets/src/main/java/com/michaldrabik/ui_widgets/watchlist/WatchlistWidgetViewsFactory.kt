package com.michaldrabik.ui_widgets.watchlist

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
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.DurationPrinter
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.replace
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_repository.shows.ShowsRepository
import com.michaldrabik.ui_watchlist.WatchlistItem
import com.michaldrabik.ui_watchlist.main.cases.WatchlistLoadItemsCase
import com.michaldrabik.ui_watchlist.main.cases.WatchlistSortOrderCase
import com.michaldrabik.ui_widgets.R
import com.michaldrabik.ui_widgets.watchlist.WatchlistWidgetProvider.Companion.EXTRA_EPISODE_ID
import com.michaldrabik.ui_widgets.watchlist.WatchlistWidgetProvider.Companion.EXTRA_SEASON_ID
import com.michaldrabik.ui_widgets.watchlist.WatchlistWidgetProvider.Companion.EXTRA_SHOW_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking

class WatchlistWidgetViewsFactory(
  private val context: Context,
  private val loadItemsCase: WatchlistLoadItemsCase,
  private val sortOrderCase: WatchlistSortOrderCase,
  private val showsRepository: ShowsRepository,
  private val imagesProvider: ShowImagesProvider
) : RemoteViewsService.RemoteViewsFactory, CoroutineScope {

  override val coroutineContext = Job() + Dispatchers.Main

  private val imageCorner by lazy { context.dimenToPx(R.dimen.showTileCorner) }
  private val imageWidth by lazy { context.dimenToPx(R.dimen.watchlistWidgetImageWidth) }
  private val imageHeight by lazy { context.dimenToPx(R.dimen.watchlistWidgetImageHeight) }
  private val adapterItems by lazy { mutableListOf<WatchlistItem>() }
  private val durationPrinter by lazy { DurationPrinter(context.applicationContext) }

  private fun loadData() {
    runBlocking {
      val shows = showsRepository.myShows.loadAll()
      val items = shows.map { show ->
        async {
          val item = loadItemsCase.loadWatchlistItem(show)
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

  private fun createItemRemoteView(item: WatchlistItem): RemoteViews {
    val subtitle = String.format("S.%02d E.%02d", item.episode.season, item.episode.number)
    val progressText = "${item.watchedEpisodesCount}/${item.episodesCount}"
    val imageUrl = "${Config.TVDB_IMAGE_BASE_BANNERS_URL}${item.image.fileUrl}"
    val hasAired = item.episode.hasAired(item.season)

    val remoteView = RemoteViews(context.packageName, R.layout.widget_watchlist_item).apply {
      setTextViewText(R.id.watchlistWidgetItemTitle, item.show.title)
      setTextViewText(R.id.watchlistWidgetItemSubtitle, subtitle)
      setTextViewText(R.id.watchlistWidgetItemSubtitle2, item.episode.title)
      setTextViewText(R.id.watchlistWidgetItemProgressText, progressText)
      setViewVisibility(R.id.watchlistWidgetItemBadge, if (item.isNew()) VISIBLE else GONE)
      setProgressBar(R.id.watchlistWidgetItemProgress, item.episodesCount, item.watchedEpisodesCount, false)
      if (hasAired) {
        setViewVisibility(R.id.watchlistWidgetItemCheckButton, VISIBLE)
        setViewVisibility(R.id.watchlistWidgetItemDateButton, GONE)
      } else {
        setViewVisibility(R.id.watchlistWidgetItemCheckButton, GONE)
        setViewVisibility(R.id.watchlistWidgetItemDateButton, VISIBLE)
        setTextViewText(R.id.watchlistWidgetItemDateButton, durationPrinter.print(item.episode.firstAired))
      }

      val fillIntent = Intent().apply {
        putExtras(
          Bundle().apply {
            putExtra(EXTRA_SHOW_ID, item.show.ids.trakt.id)
          }
        )
      }
      setOnClickFillInIntent(R.id.watchlistWidgetItem, fillIntent)

      val checkFillIntent = Intent().apply {
        putExtras(
          Bundle().apply {
            putExtra(EXTRA_EPISODE_ID, item.episode.ids.trakt.id)
            putExtra(EXTRA_SEASON_ID, item.season.ids.trakt.id)
            putExtra(EXTRA_SHOW_ID, item.show.ids.trakt.id)
          }
        )
      }
      setOnClickFillInIntent(R.id.watchlistWidgetItemCheckButton, checkFillIntent)
    }

    try {
      val bitmap = Glide.with(context).asBitmap()
        .load(imageUrl)
        .transform(CenterCrop(), RoundedCorners(imageCorner))
        .submit(imageWidth, imageHeight)
        .get()
      remoteView.setImageViewBitmap(R.id.watchlistWidgetItemImage, bitmap)
      remoteView.setViewVisibility(R.id.watchlistWidgetItemImage, VISIBLE)
    } catch (t: Throwable) {
      remoteView.setViewVisibility(R.id.watchlistWidgetItemImage, GONE)
      remoteView.setViewVisibility(R.id.watchlistWidgetItemPlaceholder, VISIBLE)
    }

    return remoteView
  }

  private fun createHeaderRemoteView(item: WatchlistItem) =
    RemoteViews(context.packageName, R.layout.widget_watchlist_header).apply {
      setTextViewText(R.id.watchlistWidgetHeaderTitle, context.getString(item.headerTextResId!!))
    }

  override fun getItemId(position: Int) = adapterItems[position].show.ids.trakt.id

  override fun onDataSetChanged() = loadData()

  override fun getLoadingView() =
    RemoteViews(context.packageName, R.layout.widget_watchlist_loading)

  override fun getCount() = adapterItems.size

  override fun hasStableIds() = true

  override fun getViewTypeCount() = 2

  override fun onDestroy() {
    coroutineContext.cancelChildren()
    adapterItems.clear()
  }
}
