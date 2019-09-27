package com.michaldrabik.showly2.ui.show.related

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Image.Status.AVAILABLE
import com.michaldrabik.showly2.ui.common.views.ShowView
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visible
import kotlinx.android.synthetic.main.view_related_show.view.*

class RelatedShowView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ShowView<RelatedListItem>(context, attrs, defStyleAttr) {

  init {
    inflate(context, R.layout.view_related_show, this)
    layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
  }

  override val imageView: ImageView = relatedImage
  override val placeholderView: ImageView = relatedPlaceholder

  override fun bind(
    item: RelatedListItem,
    missingImageListener: (RelatedListItem, Boolean) -> Unit,
    itemClickListener: (RelatedListItem) -> Unit
  ) {
    clear()
    onClick { itemClickListener(item) }
    relatedTitle.text = item.show.title

    if (!item.isLoading) loadImage(item, missingImageListener)
  }

  override fun onImageLoadFail(item: RelatedListItem, missingImageListener: (RelatedListItem, Boolean) -> Unit) {
    super.onImageLoadFail(item, missingImageListener)
    if (item.image.status == AVAILABLE) relatedTitle.visible()
  }

  private fun clear() {
    relatedTitle.text = ""
    relatedPlaceholder.gone()
    relatedTitle.gone()
    Glide.with(this).clear(relatedImage)
  }
}