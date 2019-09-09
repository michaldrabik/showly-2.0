package com.michaldrabik.showly2.ui.common.views

import android.content.Context
import android.util.AttributeSet
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.michaldrabik.showly2.Config.TVDB_IMAGE_BASE_FANART_URL
import com.michaldrabik.showly2.Config.TVDB_IMAGE_BASE_URL
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Image.Status.*
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import com.michaldrabik.showly2.utilities.extensions.*
import kotlinx.android.synthetic.main.view_show_fanart.view.*

class ShowFanartView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ShowView<DiscoverListItem>(context, attrs, defStyleAttr) {

  init {
    inflate(context, R.layout.view_show_fanart, this)
  }

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
    if (!item.isLoading) loadImage(item, missingImageListener)
  }

  private fun loadImage(item: DiscoverListItem, missingImageListener: (DiscoverListItem, Boolean) -> Unit) {
    val url = when {
      item.image.status == UNKNOWN -> "${TVDB_IMAGE_BASE_FANART_URL}${item.show.ids.tvdb}-1.jpg"
      else -> "$TVDB_IMAGE_BASE_URL${item.image.fileUrl}"
    }
    Glide.with(this)
      .load(url)
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .transition(withCrossFade(200))
      .withFailListener { onImageLoadFail(item, missingImageListener) }
      .into(showFanartImage)
  }

  private fun onImageLoadFail(item: DiscoverListItem, missingImageListener: (DiscoverListItem, Boolean) -> Unit) {
    if (item.image.status == AVAILABLE) {
      showFanartPlaceholder.visible()
      return
    }
    val force = item.image.status != UNAVAILABLE
    missingImageListener(item, force)
  }

  private fun clear() {
    showFanartTitle.text = ""
    showFanartProgress.gone()
    showFanartPlaceholder.gone()
    Glide.with(this).clear(showFanartImage)
  }
}