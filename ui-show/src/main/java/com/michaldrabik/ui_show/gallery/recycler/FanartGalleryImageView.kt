package com.michaldrabik.ui_show.gallery.recycler

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.michaldrabik.common.Config.AWS_IMAGE_BASE_URL
import com.michaldrabik.common.Config.TVDB_IMAGE_BASE_BANNERS_URL
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageSource.TVDB
import com.michaldrabik.ui_show.R
import kotlinx.android.synthetic.main.view_fanart_gallery_image.view.*

class FanartGalleryImageView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_fanart_gallery_image, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
  }

  var onItemClickListener: (() -> Unit)? = null

  fun bind(image: Image) {
    clear()
    fanartGalleryImage.onClick { onItemClickListener?.invoke() }
    fanartGalleryImageProgress.visible()
    loadImage(image)
  }

  private fun loadImage(image: Image) {
    val baseImageUrl =
      if (image.source == TVDB) TVDB_IMAGE_BASE_BANNERS_URL
      else AWS_IMAGE_BASE_URL
    Glide.with(this)
      .load("$baseImageUrl${image.fileUrl}")
      .withFailListener { fanartGalleryImageProgress.gone() }
      .withSuccessListener { fanartGalleryImageProgress.gone() }
      .into(fanartGalleryImage)
  }

  private fun clear() {
    fanartGalleryImageProgress.gone()
    Glide.with(this)
  }
}
