package com.michaldrabik.ui_search.views

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
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_search.R
import com.michaldrabik.ui_search.recycler.SearchListItem
import kotlinx.android.synthetic.main.view_show_search.view.*

@SuppressLint("SetTextI18n")
class ShowSearchView : ShowView<SearchListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_show_search, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    showSearchRoot.onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = showSearchImage

  override val placeholderView: ImageView = showSearchPlaceholder
  private lateinit var item: SearchListItem

  override fun bind(item: SearchListItem) {
    clear()
    this.item = item

    val translationTitle = item.translation?.title
    showSearchTitle.text =
      if (translationTitle.isNullOrBlank()) item.title
      else translationTitle

    val translationOverview = item.translation?.overview
    showSearchDescription.text =
      if (translationOverview.isNullOrBlank()) item.overview
      else translationOverview

    showSearchNetwork.text =
      if (item.year > 0) context.getString(R.string.textNetwork, item.network, item.year.toString())
      else String.format("%s", item.network)

    showSearchDescription.visibleIf(item.overview.isNotBlank())
    showSearchNetwork.visibleIf(item.network.isNotBlank())
    showSearchBadge.visibleIf(item.isFollowed)
    showSearchWatchlistBadge.visibleIf(item.isWatchlist)

    if (item.isMovie) showSearchPlaceholder.setImageResource(R.drawable.ic_film)
    loadImage(item)
  }

  private fun clear() {
    showSearchTitle.text = ""
    showSearchDescription.text = ""
    showSearchNetwork.text = ""
    showSearchPlaceholder.gone()
    showSearchBadge.gone()
    showSearchPlaceholder.setImageResource(R.drawable.ic_television)
    Glide.with(this).clear(showSearchImage)
  }
}
