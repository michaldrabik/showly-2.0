package com.michaldrabik.ui_people.gallery.recycler.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_people.R
import kotlinx.android.synthetic.main.view_person_gallery_image.view.*

class PersonGalleryImageView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_person_gallery_image, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
  }

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.mediaTileCorner) }
  var onItemClickListener: (() -> Unit)? = null

  fun bind(image: Image) {
    clear()
    viewPersonGalleryImage.onClick { onItemClickListener?.invoke() }
    viewPersonGalleryImageProgress.visible()
    loadImage(image)
  }

  private fun loadImage(image: Image) {
    Glide.with(this)
      .load(image.fullFileUrl)
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .withFailListener { viewPersonGalleryImageProgress.gone() }
      .withSuccessListener { viewPersonGalleryImageProgress.gone() }
      .into(viewPersonGalleryImage)
  }

  private fun clear() {
    viewPersonGalleryImageProgress.gone()
    Glide.with(this).clear(viewPersonGalleryImage)
  }
}
