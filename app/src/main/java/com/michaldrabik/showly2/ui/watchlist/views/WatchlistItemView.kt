package com.michaldrabik.showly2.ui.watchlist.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Image.Status
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.extensions.addRipple
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.expandTouchArea
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visible
import com.michaldrabik.showly2.utilities.extensions.withFailListener
import kotlinx.android.synthetic.main.view_watchlist_item.view.*

@SuppressLint("SetTextI18n")
class WatchlistItemView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.watchlistImageCorner) }

  init {
    inflate(context, R.layout.view_watchlist_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    addRipple()
    watchlistItemCheckButton.expandTouchArea(100)
  }

  fun bind(
    item: WatchlistItem,
    itemClickListener: (WatchlistItem) -> Unit,
    detailsClickListener: (WatchlistItem) -> Unit
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

    bindImage(item)
    onClick { itemClickListener(item) }
    watchlistItemInfoButton.onClick { detailsClickListener(item) }
  }

  private fun bindImage(item: WatchlistItem) {
    if (item.image.status == Status.UNAVAILABLE) {
      watchlistItemPlaceholder.visible()
      return
    }

    val base = when {
      item.image.type == POSTER -> Config.TVDB_IMAGE_BASE_POSTER_URL
      else -> Config.TVDB_IMAGE_BASE_FANART_URL
    }
    val url = when {
      item.image.status == Status.UNKNOWN -> "${base}${item.show.ids.tvdb}-1.jpg"
      else -> "${Config.TVDB_IMAGE_BASE_URL}${item.image.fileUrl}"
    }

    Glide.with(this)
      .load(url)
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .transition(withCrossFade(200))
      .withFailListener {
        watchlistItemPlaceholder.visible()
      }
      .into(watchlistItemImage)
  }

  private fun clear() {
    watchlistItemTitle.text = ""
    watchlistItemSubtitle.text = ""
    watchlistItemProgressText.text = ""
    watchlistItemPlaceholder.gone()
    Glide.with(this).clear(watchlistItemImage)
  }
}