package com.michaldrabik.showly2.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.showly2.R
import kotlinx.android.synthetic.main.view_show_poster.view.*

class ShowPosterView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  private val cornerRadius by lazy { resources.getDimensionPixelSize(R.dimen.cornerShowTile) }

  init {
    inflate(context, R.layout.view_show_poster, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  fun bind(show: Show) {
    clear()

    Glide.with(this)
      .load(getTvdbImageUrl(show.ids.tvdb))
      .error(createFallbackLoad1(show.ids.tvdb))
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .into(showTileImage)
  }

  private fun createFallbackLoad1(tvdbId: Long) =
    Glide.with(this)
      .load(getTvdbImageUrl(tvdbId, 2))
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .error(createFallbackLoad2(tvdbId))

  private fun createFallbackLoad2(tvdbId: Long) =
    Glide.with(this)
      .load(getTvdbImageUrl(tvdbId, 3))
      .transform(CenterCrop(), RoundedCorners(cornerRadius))

  private fun getTvdbImageUrl(tvdbId: Long, index: Int = 1) =
    "https://www.thetvdb.com/banners/_cache/posters/$tvdbId-$index.jpg"

  private fun clear() {
    Glide.with(this).clear(showTileImage)
  }
}