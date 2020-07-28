package com.michaldrabik.showly2.ui.watchlist.pages.upcoming.views

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
import com.michaldrabik.showly2.utilities.extensions.expandTouch
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.toDisplayString
import com.michaldrabik.showly2.utilities.extensions.toLocalTimeZone
import kotlinx.android.synthetic.main.view_watchlist_upcoming_item.view.*

@SuppressLint("SetTextI18n")
class WatchlistUpcomingItemView : ShowView<WatchlistItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var detailsClickListener: ((WatchlistItem) -> Unit)? = null
  var missingImageListener: ((WatchlistItem, Boolean) -> Unit)? = null

  init {
    inflate(context, R.layout.view_watchlist_upcoming_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    addRipple()

    onClick { itemClickListener?.invoke(item) }

    watchlistUpcomingItemInfoButton.expandTouch(100)
    watchlistUpcomingItemInfoButton.onClick { detailsClickListener?.invoke(item) }
  }

  private lateinit var item: WatchlistItem

  override val imageView: ImageView = watchlistUpcomingItemImage
  override val placeholderView: ImageView = watchlistUpcomingItemPlaceholder

  fun bind(item: WatchlistItem) {
    this.item = item
    clear()

    watchlistUpcomingItemTitle.text = item.show.title
    val episodeTitle = if (item.upcomingEpisode.title.isBlank()) "TBA" else item.upcomingEpisode.title
    watchlistUpcomingItemSubtitle.text = String.format(
      "S.%02d E.%02d",
      item.upcomingEpisode.season,
      item.upcomingEpisode.number
    )
    watchlistUpcomingItemSubtitle2.text = episodeTitle
    watchlistUpcomingItemDateText.text = item.upcomingEpisode.firstAired?.toLocalTimeZone()?.toDisplayString()

    loadImage(item, missingImageListener!!)
  }

  private fun clear() {
    watchlistUpcomingItemTitle.text = ""
    watchlistUpcomingItemSubtitle.text = ""
    watchlistUpcomingItemSubtitle2.text = ""
    watchlistUpcomingItemDateText.text = ""
    watchlistUpcomingItemPlaceholder.gone()
    Glide.with(this).clear(watchlistUpcomingItemImage)
  }
}
