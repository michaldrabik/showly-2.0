package com.michaldrabik.showly2.ui.myshows.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.visible
import kotlinx.android.synthetic.main.view_my_show_fanart.view.*

class MyShowFanartView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  init {
    inflate(context, R.layout.view_my_show_fanart, this)
  }

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.myShowFanartCorner) }

  fun bind(show: Show, image: Image) {
    clear()
    myShowFanartTitle.text = show.title
    loadImage(image)
  }

  private fun loadImage(image: Image) {
    if (image.status == Image.Status.UNAVAILABLE) {
      myShowFanartPlaceholder.visible()
      return
    }

    val url = "${Config.TVDB_IMAGE_BASE_URL}${image.fileUrl}"

    Glide.with(this)
      .load(url)
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .transition(DrawableTransitionOptions.withCrossFade(200))
      .into(myShowFanartImage)
  }

  private fun clear() {
    myShowFanartTitle.text = ""
    Glide.with(this).clear(myShowFanartImage)
  }
}