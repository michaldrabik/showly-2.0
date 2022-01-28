package com.michaldrabik.ui_people.gallery.recycler.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_people.R
import kotlinx.android.synthetic.main.view_person_gallery_image.view.*

class PersonGalleryImageView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_person_gallery_image, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
  }

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.galleryImageCorner) }
  var onItemClickListener: (() -> Unit)? = null

  fun bind(image: Image) {
    clear()
    viewPersonGalleryImage.onClick { onItemClickListener?.invoke() }
    loadImage(image)
  }

  private fun loadImage(image: Image) {
    Glide.with(this)
      .load(image.fullFileUrl)
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .into(viewPersonGalleryImage)
  }

  private fun clear() {
    Glide.with(this).clear(viewPersonGalleryImage)
  }
}
