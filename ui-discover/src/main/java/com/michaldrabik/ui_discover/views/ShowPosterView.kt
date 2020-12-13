package com.michaldrabik.ui_discover.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_discover.R
import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import com.michaldrabik.ui_model.ImageStatus.AVAILABLE
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE
import kotlinx.android.synthetic.main.view_show_poster.view.*

class ShowPosterView : ShowView<DiscoverListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_show_poster, this)
    showPosterRoot.onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = showPosterImage
  override val placeholderView: ImageView = showPosterPlaceholder

  private lateinit var item: DiscoverListItem

  override fun bind(
    item: DiscoverListItem,
    missingImageListener: ((DiscoverListItem, Boolean) -> Unit)?
  ) {
    super.bind(item, missingImageListener)
    clear()
    this.item = item
    showPosterTitle.text = item.show.title
    showPosterProgress.visibleIf(item.isLoading)
    showPosterBadge.visibleIf(item.isFollowed)
    showPosterLaterBadge.visibleIf(item.isWatchlist)
    loadImage(item, missingImageListener)
  }

  override fun loadImage(item: DiscoverListItem, missingImageListener: ((DiscoverListItem, Boolean) -> Unit)?) {
    if (item.image.status == UNAVAILABLE) {
      showPosterTitle.visible()
      showPosterRoot.setBackgroundResource(R.drawable.bg_media_view_placeholder)
    }
    super.loadImage(item, missingImageListener)
  }

  override fun onImageLoadSuccess() {
    showPosterTitle.gone()
    showPosterRoot.setBackgroundResource(0)
  }

  override fun onImageLoadFail(item: DiscoverListItem, missingImageListener: ((DiscoverListItem, Boolean) -> Unit)?) {
    super.onImageLoadFail(item, missingImageListener)
    if (item.image.status == AVAILABLE) {
      showPosterTitle.visible()
      showPosterRoot.setBackgroundResource(R.drawable.bg_media_view_placeholder)
    }
  }

  private fun clear() {
    showPosterTitle.text = ""
    showPosterTitle.gone()
    showPosterRoot.setBackgroundResource(0)
    showPosterPlaceholder.gone()
    showPosterProgress.gone()
    showPosterBadge.gone()
    Glide.with(this).clear(showPosterImage)
  }
}
