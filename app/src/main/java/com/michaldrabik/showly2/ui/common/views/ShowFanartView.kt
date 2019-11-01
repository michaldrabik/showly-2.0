package com.michaldrabik.showly2.ui.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.view_show_fanart.view.*

class ShowFanartView : ShowView<DiscoverListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_show_fanart, this)
  }

  override val imageView: ImageView = showFanartImage
  override val placeholderView: ImageView = showFanartPlaceholder

  override fun bind(
    item: DiscoverListItem,
    missingImageListener: (DiscoverListItem, Boolean) -> Unit,
    itemClickListener: (DiscoverListItem) -> Unit
  ) {
    super.bind(item, missingImageListener, itemClickListener)
    clear()
    showFanartTitle.text = item.show.title
    showFanartProgress.visibleIf(item.isLoading)
    showFanartRoot.onClick { itemClickListener(item) }
    showFanartBadge.visibleIf(item.isFollowed)
    loadImage(item, missingImageListener)
  }

  private fun clear() {
    showFanartTitle.text = ""
    showFanartProgress.gone()
    showFanartPlaceholder.gone()
    showFanartBadge.gone()
    Glide.with(this).clear(showFanartImage)
  }
}