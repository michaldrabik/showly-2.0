package com.michaldrabik.showly2.ui.common.views

import android.content.Context
import android.util.AttributeSet
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.michaldrabik.showly2.Config.TVDB_IMAGE_BASE_POSTER_URL
import com.michaldrabik.showly2.Config.TVDB_IMAGE_BASE_URL
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Image.Status.*
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import com.michaldrabik.showly2.utilities.extensions.*
import kotlinx.android.synthetic.main.view_show_poster.view.*

class ShowPosterView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ShowView<DiscoverListItem>(context, attrs, defStyleAttr) {

  init {
    inflate(context, R.layout.view_show_poster, this)
  }

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

  private fun loadImage(item: DiscoverListItem, missingImageListener: (DiscoverListItem, Boolean) -> Unit) {
    if (item.image.status == UNAVAILABLE) {
      showPosterPlaceholder.visible()
      showPosterTitle.visible()
      return
    }

    val url = when {
      item.image.status == UNKNOWN -> "${TVDB_IMAGE_BASE_POSTER_URL}${item.show.ids.tvdb}-1.jpg"
      else -> "$TVDB_IMAGE_BASE_URL${item.image.thumbnailUrl}"
    }

    Glide.with(this)
      .load(url)
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .transition(withCrossFade(200))
      .withSuccessListener { showPosterTitle.gone() }
      .withFailListener { onImageLoadFail(item, missingImageListener) }
      .into(showPosterImage)
  }

  private fun onImageLoadFail(item: DiscoverListItem, missingImageListener: (DiscoverListItem, Boolean) -> Unit) {
    if (item.image.status == AVAILABLE) {
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