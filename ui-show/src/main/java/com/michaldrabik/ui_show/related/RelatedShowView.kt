package com.michaldrabik.ui_show.related

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.ImageStatus.AVAILABLE
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE
import com.michaldrabik.ui_show.R
import kotlinx.android.synthetic.main.view_related_show.view.*

class RelatedShowView : ShowView<RelatedListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_related_show, this)
    layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    clipChildren = false
    onClick { itemClickListener?.invoke(item) }
  }

  private val colorAccent by lazy { ContextCompat.getColor(context, R.color.colorAccent) }
  private val colorGray by lazy { ContextCompat.getColor(context, R.color.colorGrayLight) }

  override val imageView: ImageView = relatedImage
  override val placeholderView: ImageView = relatedPlaceholder

  private lateinit var item: RelatedListItem

  override fun bind(item: RelatedListItem) {
    clear()
    this.item = item
    relatedTitle.text = item.show.title

    relatedBadge.visibleIf(item.isFollowed || item.isWatchlist)
    val color = if (item.isFollowed) colorAccent else colorGray
    ImageViewCompat.setImageTintList(relatedBadge, ColorStateList.valueOf(color))

    loadImage(item)
  }

  override fun loadImage(item: RelatedListItem) {
    if (item.image.status == UNAVAILABLE) {
      relatedTitle.visible()
    }
    super.loadImage(item)
  }

  override fun onImageLoadFail(item: RelatedListItem) {
    super.onImageLoadFail(item)
    if (item.image.status == AVAILABLE) {
      relatedTitle.visible()
    }
  }

  private fun clear() {
    relatedTitle.text = ""
    relatedPlaceholder.gone()
    relatedTitle.gone()
    Glide.with(this).clear(relatedImage)
  }
}
