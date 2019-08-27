package com.michaldrabik.showly2.ui.common

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.screenWidth
import kotlinx.android.synthetic.main.view_show_poster.view.*

class ShowPosterView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  private val cornerRadius by lazy { resources.getDimensionPixelSize(R.dimen.cornerShowTile) }
  private val gridSpacing by lazy { resources.getDimensionPixelSize(R.dimen.gridSpacing) }
  private val gridPadding by lazy { resources.getDimensionPixelSize(R.dimen.gridPadding) }

  init {
    inflate(context, R.layout.view_show_poster, this)
    val width = (context.screenWidth().toFloat() - (2.0 * gridSpacing) - (2.0 * gridPadding)) / 3.0
    val height = width * 1.4705
    layoutParams = LayoutParams(width.toInt(), height.toInt())
  }

  fun bind(show: Show, imageIndexMap: MutableMap<Long, Int>) {
    clear()
    loadImage(imageIndexMap, show)
  }

  private fun loadImage(
    imageIndexMap: MutableMap<Long, Int>,
    show: Show
  ) {
    val index = imageIndexMap[show.ids.tvdb] ?: 1
    if (index == -1) return

    fun createFallbackLoad2(): RequestBuilder<Drawable> {
      return Glide.with(this)
        .load(getTvdbImageUrl(show.ids.tvdb, index + 2))
        .transform(CenterCrop(), RoundedCorners(cornerRadius))
        .listener(object : RequestListener<Drawable?> {
          override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable?>?,
            isFirstResource: Boolean
          ): Boolean {
            imageIndexMap[show.ids.tvdb] = -1
            return false
          }

          override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable?>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
          ): Boolean {
            imageIndexMap[show.ids.tvdb] = index + 2
            return false
          }
        })
    }

    fun createFallbackLoad1(): RequestBuilder<Drawable> {
      return Glide.with(this)
        .load(getTvdbImageUrl(show.ids.tvdb, index + 1))
        .transform(CenterCrop(), RoundedCorners(cornerRadius))
        .listener(object : RequestListener<Drawable?> {
          override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable?>?,
            isFirstResource: Boolean
          ): Boolean {
            return false
          }

          override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable?>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
          ): Boolean {
            imageIndexMap[show.ids.tvdb] = index + 1
            return false
          }
        })
        .error(createFallbackLoad2())
    }

    Glide.with(this)
      .load(getTvdbImageUrl(show.ids.tvdb, index))
      .error(createFallbackLoad1())
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .listener(object : RequestListener<Drawable?> {
        override fun onLoadFailed(
          e: GlideException?,
          model: Any?,
          target: Target<Drawable?>?,
          isFirstResource: Boolean
        ): Boolean {
          return false
        }

        override fun onResourceReady(
          resource: Drawable?,
          model: Any?,
          target: Target<Drawable?>?,
          dataSource: DataSource?,
          isFirstResource: Boolean
        ): Boolean {
          imageIndexMap[show.ids.tvdb] = index
          return false
        }
      })
      .into(showTileImage)
  }

  private fun getTvdbImageUrl(tvdbId: Long, index: Int) =
    "https://www.thetvdb.com/banners/_cache/posters/$tvdbId-$index.jpg"

  private fun clear() {
    Glide.with(this).clear(showTileImage)
  }
}