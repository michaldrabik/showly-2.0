package com.michaldrabik.showly2.ui.show.gallery.recycler

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Image
import kotlinx.android.synthetic.main.view_fanart_gallery_image.view.*

class FanartGalleryImageView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_fanart_gallery_image, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
  }

  fun bind(
    image: Image
  ) {
    clear()
    Glide.with(this)
      .load("${Config.TVDB_IMAGE_BASE_BANNERS_URL}${image.fileUrl}")
      .into(fanartGalleryImage)
  }

  private fun clear() {
    Glide.with(this)
  }
}