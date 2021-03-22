package com.michaldrabik.ui_lists

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.invisible
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageStatus
import com.michaldrabik.ui_model.ImageType
import kotlinx.android.synthetic.main.view_triple_image.view.*

@SuppressLint("SetTextI18n")
class ListsTripleImageView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.listItemCorner) }

  init {
    inflate(context, R.layout.view_triple_image, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  fun bind(images: List<Image>) {
    clear()
    if (images.all { it.status == ImageStatus.UNAVAILABLE }) {
      return
    }
    // List is guaranteed to always have exact 3 items.
    loadImage(images[0], viewTripleImage1, viewTripleImagePlaceholder1)
    loadImage(images[1], viewTripleImage2, viewTripleImagePlaceholder2)
    loadImage(images[2], viewTripleImage3, viewTripleImagePlaceholder3)
  }

  private fun loadImage(
    image: Image,
    imageView: ImageView,
    placeholderView: ImageView
  ) {
    if (image.status == ImageStatus.UNAVAILABLE) {
      imageView.invisible()
      placeholderView.visible()
      return
    }

    val unknownBase = when (image.type) {
      ImageType.POSTER -> Config.TVDB_IMAGE_BASE_POSTER_URL
      else -> Config.TVDB_IMAGE_BASE_FANART_URL
    }
    val url = when (image.status) {
      ImageStatus.UNKNOWN -> "${unknownBase}${image.idTvdb.id}-1.jpg"
      ImageStatus.AVAILABLE -> image.fullFileUrl
      else -> error("Should not handle other statuses.")
    }

    Glide.with(this)
      .load(url)
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .transition(DrawableTransitionOptions.withCrossFade(Config.IMAGE_FADE_DURATION_MS))
      .withSuccessListener {
        imageView.visible()
        placeholderView.invisible()
      }
      .withFailListener {
        imageView.invisible()
        placeholderView.visible()
      }
      .into(imageView)
  }

  private fun clear() =
    Glide.with(this).run {
      clear(viewTripleImage1)
      clear(viewTripleImage2)
      clear(viewTripleImage3)
    }
}
