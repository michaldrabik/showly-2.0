package com.michaldrabik.showly2.ui.watchlist.pages.watchlist.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.common.views.ShowView
import com.michaldrabik.showly2.ui.watchlist.pages.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.DurationPrinter
import com.michaldrabik.showly2.utilities.extensions.addRipple
import com.michaldrabik.showly2.utilities.extensions.bump
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.expandTouch
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visible
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.view_watchlist_item.view.*

@SuppressLint("SetTextI18n")
class WatchlistItemView : ShowView<WatchlistItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var itemLongClickListener: ((WatchlistItem, View) -> Unit)? = null
  var detailsClickListener: ((WatchlistItem) -> Unit)? = null
  var checkClickListener: ((WatchlistItem) -> Unit)? = null
  var missingImageListener: ((WatchlistItem, Boolean) -> Unit)? = null

  init {
    inflate(context, R.layout.view_watchlist_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    addRipple()
    watchlistItemCheckButton.expandTouch(100)

    onClick { itemClickListener?.invoke(item) }
    setOnLongClickListener {
      itemLongClickListener?.invoke(item, watchlistItemTitle)
      true
    }
    watchlistItemInfoButton.onClick { detailsClickListener?.invoke(item) }
  }

  private lateinit var item: WatchlistItem

  override val imageView: ImageView = watchlistItemImage
  override val placeholderView: ImageView = watchlistItemPlaceholder

  private val durationPrinter by lazy { DurationPrinter(context.applicationContext) }
  private val checkButtonWidth by lazy { context.dimenToPx(R.dimen.watchlistItemCheckButtonWidth) }
  private val checkButtonHeight by lazy { context.dimenToPx(R.dimen.watchlistItemCheckButtonHeight) }

  fun bind(item: WatchlistItem) {
    this.item = item
    clear()

    watchlistItemTitle.text = item.show.title
    val episodeTitle = if (item.episode.title.isBlank()) "TBA" else item.episode.title
    watchlistItemSubtitle.text = String.format(
      "S.%02d E.%02d",
      item.episode.season,
      item.episode.number
    )
    watchlistItemSubtitle2.text = episodeTitle
    watchlistItemNewBadge.visibleIf(item.isNew())
    watchlistItemPin.visibleIf(item.isPinned)

    bindProgress(item)
    bindCheckButton(item, checkClickListener, detailsClickListener)

    loadImage(item, missingImageListener!!)
  }

  private fun bindProgress(item: WatchlistItem) {
    watchlistItemProgress.max = item.episodesCount
    watchlistItemProgress.progress = item.watchedEpisodesCount
    watchlistItemProgressText.text = "${item.watchedEpisodesCount}/${item.episodesCount}"
  }

  private fun bindCheckButton(
    item: WatchlistItem,
    checkClickListener: ((WatchlistItem) -> Unit)?,
    detailsClickListener: ((WatchlistItem) -> Unit)?
  ) {
    val hasAired = item.episode.hasAired(item.season)
    val color = if (hasAired) R.color.colorWatchlistEnabledButton else R.color.colorWatchlistDisabledButton
    if (hasAired) {
      watchlistItemInfoButton.visible()
      watchlistItemCheckButton.run {
        layoutParams = LinearLayout.LayoutParams(checkButtonWidth, checkButtonHeight)
        text = ""
        setIconResource(R.drawable.ic_check)
        onClick { it.bump { checkClickListener?.invoke(item) } }
      }
    } else {
      watchlistItemInfoButton.gone()
      watchlistItemCheckButton.run {
        layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, checkButtonHeight)
        text = durationPrinter.print(item.episode.firstAired)
        icon = null
        onClick { detailsClickListener?.invoke(item) }
      }
    }
    watchlistItemCheckButton.setTextColor(ContextCompat.getColor(context, color))
    watchlistItemCheckButton.setStrokeColorResource(color)
    watchlistItemCheckButton.setIconTintResource(color)
  }

  private fun clear() {
    watchlistItemTitle.text = ""
    watchlistItemSubtitle.text = ""
    watchlistItemSubtitle2.text = ""
    watchlistItemProgressText.text = ""
    watchlistItemPlaceholder.gone()
    Glide.with(this).clear(watchlistItemImage)
  }
}
