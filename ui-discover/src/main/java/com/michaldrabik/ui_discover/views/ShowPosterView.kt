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
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_discover.R
import com.michaldrabik.ui_discover.databinding.ViewShowPosterBinding
import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import com.michaldrabik.ui_model.ImageStatus.AVAILABLE
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE

class ShowPosterView : ShowView<DiscoverListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewShowPosterBinding.inflate(LayoutInflater.from(context), this)

  init {
    with(binding) {
      showPosterRoot.onClick { itemClickListener?.invoke(item) }
      showPosterRoot.onLongClick { itemLongClickListener?.invoke(item) }
    }
  }

  override val imageView: ImageView = binding.showPosterImage
  override val placeholderView: ImageView = binding.showPosterPlaceholder

  private lateinit var item: DiscoverListItem

  override fun bind(item: DiscoverListItem) {
    super.bind(item)
    clear()
    this.item = item
    with(binding) {
      showPosterTitle.text = item.show.title
      showPosterProgress.visibleIf(item.isLoading)
      showPosterBadge.visibleIf(item.isFollowed)
      showPosterLaterBadge.visibleIf(item.isWatchlist)
    }
    loadImage(item)
  }

  override fun loadImage(item: DiscoverListItem) {
    if (item.image.status == UNAVAILABLE) {
      with(binding) {
        showPosterTitle.visible()
        showPosterRoot.setBackgroundResource(R.drawable.bg_media_view_placeholder)
      }
    }
    super.loadImage(item)
  }

  override fun onImageLoadFail(item: DiscoverListItem) {
    super.onImageLoadFail(item)
    if (item.image.status == AVAILABLE) {
      with(binding) {
        showPosterTitle.visible()
        showPosterRoot.setBackgroundResource(R.drawable.bg_media_view_placeholder)
      }
    }
  }

  private fun clear() {
    with(binding) {
      showPosterTitle.text = ""
      showPosterTitle.gone()
      showPosterRoot.setBackgroundResource(R.drawable.bg_media_view_elevation)
      showPosterPlaceholder.gone()
      showPosterProgress.gone()
      showPosterBadge.gone()
      Glide.with(this@ShowPosterView).clear(showPosterImage)
    }
  }
}
