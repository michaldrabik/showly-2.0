package com.michaldrabik.showly2.ui.watchlist.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.common.views.ShowView
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.extensions.addRipple
import com.michaldrabik.showly2.utilities.extensions.bump
import com.michaldrabik.showly2.utilities.extensions.expandTouchArea
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.onClick
import kotlinx.android.synthetic.main.view_watchlist_item.view.*

@SuppressLint("SetTextI18n")
class WatchlistItemView : ShowView<WatchlistItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_watchlist_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    addRipple()
    watchlistItemCheckButton.expandTouchArea(100)
  }

  override val imageView: ImageView = watchlistItemImage
  override val placeholderView: ImageView = watchlistItemPlaceholder

  fun bind(
    item: WatchlistItem,
    itemClickListener: (WatchlistItem) -> Unit,
    detailsClickListener: (WatchlistItem) -> Unit,
    checkClickListener: (WatchlistItem) -> Unit,
    missingImageListener: (WatchlistItem, Boolean) -> Unit
  ) {
    clear()

    watchlistItemTitle.text = item.show.title
    val episodeTitle = if (item.episode.title.isBlank()) "TBA" else item.episode.title
    watchlistItemSubtitle.text = String.format(
      "S.%02d E.%02d - %s",
      item.episode.season,
      item.episode.number,
      episodeTitle
    )

    watchlistItemProgress.max = item.episodesCount
    watchlistItemProgress.progress = item.watchedEpisodesCount
    watchlistItemProgressText.text = "${item.watchedEpisodesCount}/${item.episodesCount}"

    loadImage(item, missingImageListener)

    val color = if (item.episode.hasAired()) R.color.colorWatchlistEnabledButton else R.color.colorWatchlistDisabledButton
    watchlistItemCheckButton.setStrokeColorResource(color)
    watchlistItemCheckButton.setIconTintResource(color)

    onClick { itemClickListener(item) }
    watchlistItemCheckButton.onClick { it.bump { checkClickListener(item) } }
    watchlistItemInfoButton.onClick { detailsClickListener(item) }
  }

  private fun clear() {
    watchlistItemTitle.text = ""
    watchlistItemSubtitle.text = ""
    watchlistItemProgressText.text = ""
    watchlistItemPlaceholder.gone()
    Glide.with(this).clear(watchlistItemImage)
  }
}