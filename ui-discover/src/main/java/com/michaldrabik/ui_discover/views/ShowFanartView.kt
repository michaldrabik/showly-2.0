package com.michaldrabik.ui_discover.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_discover.R
import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import com.michaldrabik.ui_model.ImageStatus
import kotlinx.android.synthetic.main.view_show_fanart.view.*

class ShowFanartView : ShowView<DiscoverListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_show_fanart, this)
    showFanartRoot.onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = showFanartImage
  override val placeholderView: ImageView = showFanartPlaceholder

  private lateinit var item: DiscoverListItem

  override fun bind(
    item: DiscoverListItem,
    missingImageListener: ((DiscoverListItem, Boolean) -> Unit)?
  ) {
    super.bind(item, missingImageListener)
    clear()
    this.item = item
    showFanartTitle.text =
      if (item.translation?.title.isNullOrBlank()) item.show.title
      else item.translation?.title?.capitalizeWords()
    showFanartProgress.visibleIf(item.isLoading)
    showFanartBadge.visibleIf(item.isFollowed)
    showFanartBadgeLater.visibleIf(item.isWatchlist)
    loadImage(item, missingImageListener)
  }

  override fun loadImage(item: DiscoverListItem, missingImageListener: ((DiscoverListItem, Boolean) -> Unit)?) {
    super.loadImage(item, missingImageListener)
    if (item.image.status == ImageStatus.UNAVAILABLE) {
      showFanartRoot.setBackgroundResource(R.drawable.bg_media_view_placeholder)
    }
  }

  private fun clear() {
    showFanartTitle.text = ""
    showFanartProgress.gone()
    showFanartPlaceholder.gone()
    showFanartRoot.setBackgroundResource(0)
    showFanartBadge.gone()
    Glide.with(this).clear(showFanartImage)
  }
}
