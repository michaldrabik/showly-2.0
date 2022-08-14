package com.michaldrabik.ui_lists.lists.views

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
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.invisible
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.lists.helpers.ListsItemImage
import com.michaldrabik.ui_model.ImageStatus
import kotlinx.android.synthetic.main.view_triple_image.view.*

@SuppressLint("SetTextI18n")
class ListsTripleImageView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.mediaTileCorner) }

  var missingImageListener: ((ListsItemImage, Boolean) -> Unit)? = null

  init {
    inflate(context, R.layout.view_triple_image, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  fun bind(images: List<ListsItemImage>) {
    clear()
    if (images.all { it.image.status == ImageStatus.UNAVAILABLE }) {
      viewTripleImagePlaceholder1.visible()
      viewTripleImagePlaceholder1.visible()
      viewTripleImagePlaceholder2.visible()
      viewTripleImagePlaceholder3.visible()
      viewTripleImage1.gone()
      viewTripleImage2.gone()
      viewTripleImage3.gone()
      return
    }
    // List is guaranteed to always have exact 3 items.
    loadImage(images[0], viewTripleImage1, viewTripleImagePlaceholder1)
    loadImage(images[1], viewTripleImage2, viewTripleImagePlaceholder2)
    loadImage(images[2], viewTripleImage3, viewTripleImagePlaceholder3)
  }

  private fun loadImage(
    itemImage: ListsItemImage,
    imageView: ImageView,
    placeholderView: ImageView,
  ) {
    if (itemImage.image.status == ImageStatus.UNAVAILABLE) {
      imageView.invisible()
      placeholderView.visible()
      return
    }
    if (itemImage.image.status == ImageStatus.UNKNOWN) {
      missingImageListener?.invoke(itemImage, true)
      return
    }

    Glide.with(this)
      .load(itemImage.image.fullFileUrl)
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .transition(DrawableTransitionOptions.withCrossFade(Config.IMAGE_FADE_DURATION_MS))
      .withSuccessListener {
        imageView.visible()
        placeholderView.invisible()
      }
      .withFailListener {
        if (itemImage.image.status == ImageStatus.AVAILABLE) {
          imageView.invisible()
          placeholderView.visible()
          return@withFailListener
        }
        val force = (itemImage.image.status == ImageStatus.UNKNOWN)
        missingImageListener?.invoke(itemImage, force)
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
