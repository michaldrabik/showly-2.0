package com.michaldrabik.showly2.ui.search.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.common.views.ShowView
import com.michaldrabik.showly2.ui.search.recycler.SearchListItem
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.view_show_search.view.*

@SuppressLint("SetTextI18n")
class ShowSearchView : ShowView<SearchListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_show_search, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  override val imageView: ImageView = showSearchImage
  override val placeholderView: ImageView = showSearchPlaceholder

  override fun bind(
    item: SearchListItem,
    missingImageListener: (SearchListItem, Boolean) -> Unit,
    itemClickListener: (SearchListItem) -> Unit
  ) {
    clear()
    showSearchTitle.text = item.show.title
    showSearchDescription.text = item.show.overview
    val year = if (item.show.year > 0) " (${item.show.year})" else ""
    showSearchNetwork.text = "${item.show.network}$year"

    showSearchDescription.visibleIf(item.show.overview.isNotBlank())
    showSearchNetwork.visibleIf(item.show.network.isNotBlank())
    showSearchBadge.visibleIf(item.isFollowed)
    loadImage(item, missingImageListener)

    showSearchRoot.onClick { itemClickListener(item) }
  }

  private fun clear() {
    showSearchTitle.text = ""
    showSearchDescription.text = ""
    showSearchNetwork.text = ""
    showSearchPlaceholder.gone()
    showSearchBadge.gone()
    Glide.with(this).clear(showSearchImage)
  }
}