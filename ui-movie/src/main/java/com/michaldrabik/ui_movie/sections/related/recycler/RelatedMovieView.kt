package com.michaldrabik.ui_movie.sections.related.recycler

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.ImageStatus.AVAILABLE
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE
import com.michaldrabik.ui_movie.R
import com.michaldrabik.ui_movie.databinding.ViewRelatedMovieBinding

class RelatedMovieView : MovieView<RelatedListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewRelatedMovieBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    clipChildren = false
    onClick { itemClickListener?.invoke(item) }
    onLongClick { itemLongClickListener?.invoke(item) }
  }

  private val colorAccent by lazy { ContextCompat.getColor(context, R.color.colorAccent) }
  private val colorGray by lazy { ContextCompat.getColor(context, R.color.colorGrayLight) }

  override val imageView: ImageView = binding.relatedMovieImage
  override val placeholderView: ImageView = binding.relatedMoviePlaceholder

  private lateinit var item: RelatedListItem

  override fun bind(item: RelatedListItem) {
    clear()
    this.item = item

    binding.relatedMovieTitle.text = item.movie.title
    binding.relatedMovieBadge.visibleIf(item.isFollowed || item.isWatchlist)

    ImageViewCompat.setImageTintList(
      binding.relatedMovieBadge,
      ColorStateList.valueOf(if (item.isFollowed) colorAccent else colorGray)
    )

    loadImage(item)
  }

  override fun loadImage(item: RelatedListItem) {
    if (item.image.status == UNAVAILABLE) {
      binding.relatedMovieTitle.visible()
    }
    super.loadImage(item)
  }

  override fun onImageLoadFail(item: RelatedListItem) {
    super.onImageLoadFail(item)
    if (item.image.status == AVAILABLE) {
      binding.relatedMovieTitle.visible()
    }
  }

  private fun clear() {
    with(binding) {
      relatedMoviePlaceholder.gone()
      relatedMovieTitle.gone()
      Glide.with(this@RelatedMovieView)
        .clear(relatedMovieImage)
    }
  }
}
