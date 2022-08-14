package com.michaldrabik.ui_base.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.michaldrabik.common.Config.IMAGE_FADE_DURATION_MS
import com.michaldrabik.common.Config.MAIN_GRID_SPAN
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.MovieListItem
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.screenWidth
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_model.ImageStatus.AVAILABLE
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE
import com.michaldrabik.ui_model.ImageStatus.UNKNOWN

abstract class MovieView<Item : MovieListItem> : FrameLayout {

  companion object {
    const val ASPECT_RATIO = 1.4705
  }

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.mediaTileCorner) }
  private val gridPadding by lazy { context.dimenToPx(R.dimen.gridPadding) }
  private val centerCropTransformation by lazy { CenterCrop() }
  private val cornersTransformation by lazy { RoundedCorners(cornerRadius) }

  private val width by lazy { (screenWidth().toFloat() - (2.0 * gridPadding)) / MAIN_GRID_SPAN }
  private val height by lazy { width * ASPECT_RATIO }

  protected abstract val imageView: ImageView
  protected abstract val placeholderView: ImageView

  var itemClickListener: ((Item) -> Unit)? = null
  var itemLongClickListener: ((Item) -> Unit)? = null
  var imageLoadCompleteListener: (() -> Unit)? = null
  var missingImageListener: ((Item, Boolean) -> Unit)? = null
  var missingTranslationListener: ((Item) -> Unit)? = null

  open fun bind(item: Item) {
    layoutParams = LayoutParams((width * item.image.type.spanSize.toFloat()).toInt(), height.toInt())
  }

  protected open fun loadImage(item: Item) {
    if (item.isLoading) return

    if (item.image.status == UNAVAILABLE) {
      placeholderView.visible()
      return
    }

    if (item.image.status == UNKNOWN) {
      onImageLoadFail(item)
      return
    }

    Glide.with(this)
      .load(item.image.fullFileUrl)
      .transform(centerCropTransformation, cornersTransformation)
      .transition(withCrossFade(IMAGE_FADE_DURATION_MS))
      .withSuccessListener { onImageLoadSuccess() }
      .withFailListener { onImageLoadFail(item) }
      .into(imageView)
  }

  protected open fun onImageLoadSuccess() = imageLoadCompleteListener?.invoke()

  protected open fun onImageLoadFail(item: Item) {
    if (item.image.status == AVAILABLE) {
      placeholderView.visible()
      imageLoadCompleteListener?.invoke()
      return
    }
    val force = (item.image.status == UNKNOWN)
    missingImageListener?.invoke(item, force)
  }
}
