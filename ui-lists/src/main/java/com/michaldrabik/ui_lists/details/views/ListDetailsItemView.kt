package com.michaldrabik.ui_lists.details.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.michaldrabik.common.Config.IMAGE_FADE_DURATION_MS
import com.michaldrabik.common.Config.TVDB_IMAGE_BASE_FANART_URL
import com.michaldrabik.common.Config.TVDB_IMAGE_BASE_POSTER_URL
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import com.michaldrabik.ui_model.ImageStatus.AVAILABLE
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE
import com.michaldrabik.ui_model.ImageStatus.UNKNOWN
import com.michaldrabik.ui_model.ImageType.POSTER

@SuppressLint("ClickableViewAccessibility")
abstract class ListDetailsItemView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.mediaTileCorner) }
  private val centerCropTransformation by lazy { CenterCrop() }
  private val cornersTransformation by lazy { RoundedCorners(cornerRadius) }

  protected abstract val imageView: ImageView
  protected abstract val placeholderView: ImageView

  var itemClickListener: ((ListDetailsItem) -> Unit)? = null
  var imageLoadCompleteListener: (() -> Unit)? = null
  var missingImageListener: ((ListDetailsItem, Boolean) -> Unit)? = null
  var missingTranslationListener: ((ListDetailsItem) -> Unit)? = null
  var itemDragStartListener: (() -> Unit)? = null
  var itemSwipeStartListener: (() -> Unit)? = null

  lateinit var item: ListDetailsItem

  open fun bind(item: ListDetailsItem) {
    this.item = item
  }

  protected open fun loadImage(item: ListDetailsItem) {
    if (item.isLoading) return

    if (item.image.status == UNAVAILABLE) {
      placeholderView.visible()
      return
    }

    val unknownBase = when (item.image.type) {
      POSTER -> TVDB_IMAGE_BASE_POSTER_URL
      else -> TVDB_IMAGE_BASE_FANART_URL
    }
    val url = when (item.image.status) {
      UNKNOWN -> {
        when {
          item.isShow() -> "${unknownBase}${item.show?.ids?.tvdb?.id}-1.jpg"
          item.isMovie() -> "${unknownBase}${item.movie?.ids?.tvdb?.id}-1.jpg"
          else -> throw IllegalStateException()
        }
      }
      AVAILABLE -> item.image.fullFileUrl
      else -> error("Should not handle other statuses.")
    }

    Glide.with(this)
      .load(url)
      .transform(centerCropTransformation, cornersTransformation)
      .transition(withCrossFade(IMAGE_FADE_DURATION_MS))
      .withSuccessListener { onImageLoadSuccess() }
      .withFailListener { onImageLoadFail(item) }
      .into(imageView)
  }

  protected open fun onImageLoadSuccess() {
    placeholderView.gone()
    imageLoadCompleteListener?.invoke()
  }

  protected open fun onImageLoadFail(item: ListDetailsItem) {
    if (item.image.status == AVAILABLE) {
      placeholderView.visible()
      imageLoadCompleteListener?.invoke()
      return
    }
    val force = (item.image.status == UNKNOWN)
    missingImageListener?.invoke(item, force)
  }
}
