package com.michaldrabik.ui_movie.related

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_model.ImageStatus.AVAILABLE
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE
import com.michaldrabik.ui_movie.R
import kotlinx.android.synthetic.main.view_related_movie.view.*

class RelatedMovieView : MovieView<RelatedListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_related_movie, this)
    layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = relatedMovieImage
  override val placeholderView: ImageView = relatedMoviePlaceholder

  private lateinit var item: RelatedListItem

  override fun bind(
    item: RelatedListItem,
    missingImageListener: ((RelatedListItem, Boolean) -> Unit)?
  ) {
    clear()
    this.item = item
    relatedMovieTitle.text = item.movie.title
    loadImage(item, missingImageListener)
  }

  override fun loadImage(item: RelatedListItem, missingImageListener: ((RelatedListItem, Boolean) -> Unit)?) {
    if (item.image.status == UNAVAILABLE) {
      relatedMovieTitle.visible()
      relatedMovieRoot.setBackgroundResource(R.drawable.bg_media_view_placeholder)
    }
    super.loadImage(item, missingImageListener)
  }

  override fun onImageLoadFail(item: RelatedListItem, missingImageListener: ((RelatedListItem, Boolean) -> Unit)?) {
    super.onImageLoadFail(item, missingImageListener)
    if (item.image.status == AVAILABLE) {
      relatedMovieTitle.visible()
    }
  }

  private fun clear() {
    relatedMovieTitle.text = ""
    relatedMoviePlaceholder.gone()
    relatedMovieTitle.gone()
    Glide.with(this).clear(relatedMovieImage)
  }
}
