package com.michaldrabik.showly2.ui.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Image.Status.UNAVAILABLE
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visible
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.view_show_poster.view.*

class ShowPosterView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ShowView<DiscoverListItem>(context, attrs, defStyleAttr) {

  init {
    inflate(context, R.layout.view_show_poster, this)
  }

  override val imageView: ImageView = showPosterImage
  override val placeholderView: ImageView = showPosterPlaceholder

  override fun bind(
    item: DiscoverListItem,
    missingImageListener: (DiscoverListItem, Boolean) -> Unit,
    itemClickListener: (DiscoverListItem) -> Unit
  ) {
    super.bind(item, missingImageListener, itemClickListener)
    clear()
    showPosterTitle.text = item.show.title
    showPosterProgress.visibleIf(item.isLoading)
    showPosterRoot.onClick { itemClickListener(item) }
    if (!item.isLoading) loadImage(item, missingImageListener)
  }

  override fun loadImage(item: DiscoverListItem, missingImageListener: (DiscoverListItem, Boolean) -> Unit) {
    if (item.image.status == UNAVAILABLE) {
      showPosterTitle.visible()
    }
    super.loadImage(item, missingImageListener)
  }

  override fun onImageLoadSuccess() {
    showPosterTitle.gone()
  }

  override fun onImageLoadFail(item: DiscoverListItem, missingImageListener: (DiscoverListItem, Boolean) -> Unit) {
    if (item.image.status == UNAVAILABLE) {
      showPosterPlaceholder.visible()
      showPosterTitle.visible()
      return
    }
    val force = item.image.status != UNAVAILABLE
    missingImageListener(item, force)
  }

  private fun clear() {
    showPosterTitle.text = ""
    showPosterTitle.gone()
    showPosterPlaceholder.gone()
    showPosterProgress.gone()
    Glide.with(this).clear(showPosterImage)
  }
}