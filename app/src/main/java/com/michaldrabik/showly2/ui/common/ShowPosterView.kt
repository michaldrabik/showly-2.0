package com.michaldrabik.showly2.ui.common

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.gone
import com.michaldrabik.showly2.utilities.screenWidth
import com.michaldrabik.showly2.utilities.visible
import com.michaldrabik.showly2.utilities.withFailListener
import com.michaldrabik.showly2.utilities.withSuccessListener
import kotlinx.android.synthetic.main.view_show_poster.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren

class ShowPosterView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  companion object {
    private const val ASPECT_RATIO = 1.4705
  }

  private val job = Job()
  private val scope = CoroutineScope(Dispatchers.Default + job)

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

  fun bind(show: Show, imageIndexMap: MutableMap<Long, Int>) {
    clear()
    showTileTitle.text = show.title
    loadImage(imageIndexMap, show)
  }

  //TODO Refactor to TVDB calls
  private fun loadImage(
    imageIndexMap: MutableMap<Long, Int>,
    show: Show
  ) {
    val index = imageIndexMap[show.ids.tvdb] ?: 1
    if (index == -1) {
      showTileTitle.visible()
      return
    }

    fun createFallbackLoad2(): RequestBuilder<Drawable> {
      return Glide.with(this)
        .load(getTvdbImageUrl(show.ids.tvdb, index + 2))
        .transform(CenterCrop(), RoundedCorners(cornerRadius))
        .withSuccessListener { imageIndexMap[show.ids.tvdb] = index + 2 }
        .withFailListener {
          imageIndexMap[show.ids.tvdb] = -1
          showTileTitle.visible()
        }
    }

    fun createFallbackLoad1(): RequestBuilder<Drawable> {
      return Glide.with(this)
        .load(getTvdbImageUrl(show.ids.tvdb, index + 1))
        .transform(CenterCrop(), RoundedCorners(cornerRadius))
        .withSuccessListener { imageIndexMap[show.ids.tvdb] = index + 1 }
        .error(createFallbackLoad2())
    }

    Glide.with(this)
      .load(getTvdbImageUrl(show.ids.tvdb, index))
      .error(createFallbackLoad1())
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .withSuccessListener { imageIndexMap[show.ids.tvdb] = index }
      .into(showTileImage)
  }

  private fun getTvdbImageUrl(tvdbId: Long, index: Int) =
    "https://www.thetvdb.com/banners/_cache/posters/$tvdbId-$index.jpg"

  private fun clear() {
    showTileTitle.text = ""
    showTileTitle.gone()
    Glide.with(this).clear(showTileImage)
    scope.coroutineContext.cancelChildren()
  }

  override fun onDetachedFromWindow() {
    scope.coroutineContext.cancelChildren()
    super.onDetachedFromWindow()
  }
}