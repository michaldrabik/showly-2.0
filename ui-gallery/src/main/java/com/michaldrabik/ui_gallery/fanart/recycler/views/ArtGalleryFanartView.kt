package com.michaldrabik.ui_gallery.fanart.recycler.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_gallery.databinding.ViewGalleryFanartImageBinding
import com.michaldrabik.ui_model.Image

class ArtGalleryFanartView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewGalleryFanartImageBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
  }

  var onItemClickListener: (() -> Unit)? = null

  fun bind(image: Image) {
    clear()
    with(binding) {
      viewGalleryFanarImage.onClick { onItemClickListener?.invoke() }
      viewGalleryFanarImageProgress.visible()
    }
    loadImage(image)
  }

  private fun loadImage(image: Image) {
    with(binding) {
      Glide.with(this@ArtGalleryFanartView)
        .load(image.fullFileUrl)
        .withFailListener { viewGalleryFanarImageProgress.gone() }
        .withSuccessListener { viewGalleryFanarImageProgress.gone() }
        .into(viewGalleryFanarImage)
    }
  }

  private fun clear() {
    binding.viewGalleryFanarImageProgress.gone()
    Glide.with(this)
  }
}
