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
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visible
import kotlinx.android.synthetic.main.view_my_show.view.*

class MyShowFanartView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_my_show, this)
  }

  var onItemClickListener: (Show) -> Unit = {}

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.myShowsFanartCorner) }

  fun bind(show: Show, image: Image) {
    clear()
    myShowTitle.text = show.title
    myShowTitle.visible()
    onClick { onItemClickListener(show) }
    loadImage(image)
  }

  private fun loadImage(image: Image) {
    if (image.status != Image.Status.AVAILABLE) {
      myShowPlaceholder.visible()
      return
    }

    val url = "${Config.TVDB_IMAGE_BASE_URL}${image.fileUrl}"

    Glide.with(this)
      .load(url)
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .transition(DrawableTransitionOptions.withCrossFade(200))
      .into(myShowImage)
  }

  private fun clear() {
    myShowTitle.text = ""
    Glide.with(this).clear(myShowImage)
  }
}