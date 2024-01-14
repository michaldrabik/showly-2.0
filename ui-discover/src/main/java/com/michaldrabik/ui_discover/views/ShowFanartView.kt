package com.michaldrabik.ui_discover.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_discover.R
import com.michaldrabik.ui_discover.databinding.ViewShowFanartBinding
import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import com.michaldrabik.ui_model.ImageStatus.AVAILABLE
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE

class ShowFanartView : ShowView<DiscoverListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewShowFanartBinding.inflate(LayoutInflater.from(context), this)

  init {
    with(binding) {
      showFanartRoot.onClick { itemClickListener?.invoke(item) }
      showFanartRoot.onLongClick { itemLongClickListener?.invoke(item) }
    }
  }

  override val imageView: ImageView = binding.showFanartImage
  override val placeholderView: ImageView = binding.showFanartPlaceholder

  private lateinit var item: DiscoverListItem

  override fun bind(item: DiscoverListItem) {
    super.bind(item)
    clear()
    this.item = item
    with(binding) {
      showFanartTitle.text =
        if (item.translation?.title.isNullOrBlank()) item.show.title
        else item.translation?.title
      showFanartProgress.visibleIf(item.isLoading)
      showFanartBadge.visibleIf(item.isFollowed)
      showFanartBadgeLater.visibleIf(item.isWatchlist)
    }
    loadImage(item)
  }

  override fun loadImage(item: DiscoverListItem) {
    super.loadImage(item)
    if (item.image.status == UNAVAILABLE) {
      binding.showFanartRoot.setBackgroundResource(R.drawable.bg_media_view_placeholder)
    }
  }

  override fun onImageLoadFail(item: DiscoverListItem) {
    super.onImageLoadFail(item)
    if (item.image.status == AVAILABLE) {
      binding.showFanartRoot.setBackgroundResource(R.drawable.bg_media_view_placeholder)
    }
  }

  private fun clear() {
    with(binding) {
      showFanartTitle.text = ""
      showFanartProgress.gone()
      showFanartPlaceholder.gone()
      showFanartRoot.setBackgroundResource(R.drawable.bg_media_view_elevation)
      showFanartBadge.gone()
      Glide.with(this@ShowFanartView).clear(showFanartImage)
    }
  }
}
