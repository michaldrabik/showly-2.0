package com.michaldrabik.showly2.ui.watchlist.views

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
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import kotlinx.android.synthetic.main.view_watchlist_item.view.*

class WatchlistItemView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.watchlistImageCorner) }

  init {
    inflate(context, R.layout.view_watchlist_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  fun bind(item: WatchlistItem) {
    clear()

    watchlistItemTitle.text = item.show.title
    val episodeTitle = if (item.episode.title.isBlank()) "TBA" else item.episode.title
    watchlistItemSubtitle.text = String.format(
      "S.%02d E.%02d - %s",
      item.episode.season,
      item.episode.number,
      episodeTitle
    )
    bindImage(item)
  }

  private fun bindImage(item: WatchlistItem) {
    if (item.image.status == Status.UNAVAILABLE) {
//      placeholderView.visible()
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
      .into(watchlistItemImage)
  }

  private fun clear() {
    watchlistItemTitle.text = ""
    watchlistItemSubtitle.text = ""
    Glide.with(this).clear(watchlistItemImage)
  }
}