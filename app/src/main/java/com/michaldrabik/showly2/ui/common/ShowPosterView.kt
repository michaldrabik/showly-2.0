package com.michaldrabik.showly2.ui.common

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.michaldrabik.showly2.Config.TVDB_IMAGE_BASE_URL
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.ImageUrl.Status.UNAVAILABLE
import com.michaldrabik.showly2.model.ImageUrl.Status.UNKNOWN
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import com.michaldrabik.showly2.utilities.*
import kotlinx.android.synthetic.main.view_show_poster.view.*

class ShowPosterView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  companion object {
    private const val ASPECT_RATIO = 1.4705
  }

  private val cornerRadius by lazy { resources.getDimensionPixelSize(R.dimen.cornerShowTile) }
  private val gridSpacing by lazy { resources.getDimensionPixelSize(R.dimen.gridSpacing) }
  private val gridPadding by lazy { resources.getDimensionPixelSize(R.dimen.gridPadding) }
  private val gridSpan by lazy { resources.getInteger(R.integer.discoverGridSpan).toFloat() }

  init {
    inflate(context, R.layout.view_show_poster, this)
    val width = (screenWidth().toFloat() - (2.0 * gridSpacing) - (2.0 * gridPadding)) / gridSpan
    val height = width * ASPECT_RATIO
    layoutParams = LayoutParams(width.toInt(), height.toInt())
  }

  fun bind(item: DiscoverListItem, missingImageListener: (DiscoverListItem, Boolean) -> Unit) {
    clear()
    showTileTitle.text = item.show.title
    showTileProgress.visibleIf(item.isLoading)
    if (!item.isLoading) loadImage(item, missingImageListener)
  }

  private fun loadImage(item: DiscoverListItem, missingImageListener: (DiscoverListItem, Boolean) -> Unit) {
    if (item.imageUrl.status == UNAVAILABLE) {
      showTileTitle.visible()
      return
    }

    val url = when {
      item.imageUrl.status == UNKNOWN -> "${TVDB_IMAGE_BASE_URL}_cache/posters/${item.show.ids.tvdb}-1.jpg"
      else -> "$TVDB_IMAGE_BASE_URL${item.imageUrl.url}"
    }
    Glide.with(this)
      .load(url)
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .transition(withCrossFade(200))
      .withFailListener { onImageLoadFail(item, missingImageListener) }
      .into(showTileImage)
  }

  private fun onImageLoadFail(item: DiscoverListItem, missingImageListener: (DiscoverListItem, Boolean) -> Unit) {
    val force = item.imageUrl.status != UNAVAILABLE
    missingImageListener(item, force)
  }

  private fun clear() {
    showTileTitle.text = ""
    showTileTitle.gone()
    showTileProgress.gone()
    Glide.with(this).clear(showTileImage)
  }
}