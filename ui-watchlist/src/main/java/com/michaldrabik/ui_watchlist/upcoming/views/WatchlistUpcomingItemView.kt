package com.michaldrabik.ui_watchlist.upcoming.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.common.extensions.toDisplayString
import com.michaldrabik.common.extensions.toLocalTimeZone
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.addRipple
import com.michaldrabik.ui_base.utilities.extensions.expandTouch
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_watchlist.R
import com.michaldrabik.ui_watchlist.WatchlistItem
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
    watchlistUpcomingItemDateText.text = item.upcomingEpisode.firstAired?.toLocalTimeZone()?.toDisplayString()

    val isNewSeason = item.upcomingEpisode.number == 1
    if (isNewSeason) {
      watchlistUpcomingItemSubtitle2.text = context.getString(R.string.textSeason, item.upcomingEpisode.season)
      watchlistUpcomingItemSubtitle.text = context.getString(R.string.textNewSeason)
    } else {
      val subtitle = if (item.upcomingEpisode.title.isBlank()) "TBA" else item.upcomingEpisode.title
      watchlistUpcomingItemSubtitle2.text = subtitle
      watchlistUpcomingItemSubtitle.text = String.format(
        "S.%02d E.%02d",
        item.upcomingEpisode.season,
        item.upcomingEpisode.number
      )
    }

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
