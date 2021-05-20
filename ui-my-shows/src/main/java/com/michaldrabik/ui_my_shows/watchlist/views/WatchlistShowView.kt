package com.michaldrabik.ui_my_shows.watchlist.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.watchlist.recycler.WatchlistListItem
import kotlinx.android.synthetic.main.view_watchlist_show.view.*
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class WatchlistShowView : ShowView<WatchlistListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_watchlist_show, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    watchlistShowRoot.onClick { itemClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = watchlistShowImage
  override val placeholderView: ImageView = watchlistShowPlaceholder

  private lateinit var item: WatchlistListItem

  override fun bind(item: WatchlistListItem) {
    clear()
    this.item = item
    watchlistShowProgress.visibleIf(item.isLoading)
    watchlistShowTitle.text =
      if (item.translation?.title.isNullOrBlank()) item.show.title
      else item.translation?.title

    watchlistShowDescription.text =
      if (item.translation?.overview.isNullOrBlank()) item.show.overview
      else item.translation?.overview

    watchlistShowNetwork.text =
      if (item.show.year > 0) context.getString(R.string.textNetwork, item.show.network, item.show.year.toString())
      else String.format("%s", item.show.network)

    watchlistShowRating.text = String.format(ENGLISH, "%.1f", item.show.rating)
    watchlistShowDescription.visibleIf(item.show.overview.isNotBlank())
    watchlistShowNetwork.visibleIf(item.show.network.isNotBlank())

    item.userRating?.let {
      watchlistShowUserStarIcon.visible()
      watchlistShowUserRating.visible()
      watchlistShowUserRating.text = String.format(ENGLISH, "%d", it)
    }

    loadImage(item)
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    watchlistShowTitle.text = ""
    watchlistShowDescription.text = ""
    watchlistShowNetwork.text = ""
    watchlistShowRating.text = ""
    watchlistShowPlaceholder.gone()
    watchlistShowUserRating.gone()
    watchlistShowUserStarIcon.gone()
    Glide.with(this).clear(watchlistShowImage)
  }
}
