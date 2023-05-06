package com.michaldrabik.ui_movie.sections.collections.details.recycler.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.michaldrabik.common.Config.IMAGE_FADE_DURATION_MS
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.invisible
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_model.ImageStatus
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_movie.R
import com.michaldrabik.ui_movie.databinding.ViewMovieCollectionListItemBinding
import com.michaldrabik.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem
import com.michaldrabik.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem.MovieItem

class MovieDetailsCollectionItemView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewMovieCollectionListItemBinding.inflate(LayoutInflater.from(context), this)

  var onItemClickListener: ((MovieDetailsCollectionItem) -> Unit)? = null
  var onItemLongClickListener: ((MovieDetailsCollectionItem) -> Unit)? = null
  var onMissingImageListener: ((MovieDetailsCollectionItem, Boolean) -> Unit)? = null
  var onMissingTranslationListener: ((MovieDetailsCollectionItem) -> Unit)? = null

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.mediaTileCorner) }
  private val centerCropTransformation by lazy { CenterCrop() }
  private val cornersTransformation by lazy { RoundedCorners(cornerRadius) }

  private val colorAccent by lazy { ContextCompat.getColor(context, R.color.colorAccent) }
  private val colorGray by lazy { ContextCompat.getColor(context, R.color.colorGrayLight) }

  private lateinit var item: MovieDetailsCollectionItem

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    with(binding.rootLayout) {
      onClick { onItemClickListener?.invoke(item) }
      onLongClick { onItemLongClickListener?.invoke(item) }
    }
  }

  fun bind(item: MovieItem) {
    clear()
    this.item = item

    bindTitleDescription(item.movie.title, item.movie.overview, item.translation)
    bindBadge(item.isMyMovie, item.isWatchlist)

    binding.headerText.text = String.format("%s", item.movie.released?.year ?: "TBA")
    binding.rankText.text = item.rank.toString()

    if (!item.isLoading) loadImage(item)
  }

  private fun bindTitleDescription(
    title: String,
    description: String,
    translation: Translation?,
  ) {
    with(binding) {
      titleText.text = when {
        translation?.title?.isNotBlank() == true -> translation.title
        else -> title
      }
      descriptionText.text = when {
        translation?.overview?.isNotBlank() == true -> translation.overview
        description.isNotBlank() -> description
        else -> context.getString(R.string.textNoDescription)
      }
    }
  }

  private fun bindBadge(isMyMovie: Boolean, isWatchlist: Boolean) {
    with(binding) {
      badgeImage.visibleIf(isMyMovie || isWatchlist)
      ImageViewCompat.setImageTintList(
        badgeImage,
        ColorStateList.valueOf(if (isMyMovie) colorAccent else colorGray)
      )
    }
  }

  private fun loadImage(item: MovieDetailsCollectionItem) {
    val image = when (item) {
      is MovieItem -> item.image
      else -> throw IllegalArgumentException()
    }

    if (image.status == ImageStatus.UNAVAILABLE) {
      binding.movieImage.invisible()
      binding.placeholderImage.fadeIn(IMAGE_FADE_DURATION_MS.toLong())
      return
    }

    if (image.status == ImageStatus.UNKNOWN) {
      onMissingImageListener?.invoke(item, true)
      return
    }

    Glide.with(this)
      .load(image.fullFileUrl)
      .transform(centerCropTransformation, cornersTransformation)
      .transition(DrawableTransitionOptions.withCrossFade(IMAGE_FADE_DURATION_MS))
      .withSuccessListener {
        binding.placeholderImage.invisible()
        loadTranslation(item)
      }
      .withFailListener {
        if (image.status == ImageStatus.AVAILABLE) {
          binding.movieImage.invisible()
          binding.placeholderImage.fadeIn(IMAGE_FADE_DURATION_MS.toLong())
          loadTranslation(item)
          return@withFailListener
        }
        onMissingImageListener?.invoke(item, false)
      }
      .into(binding.movieImage)
  }

  private fun loadTranslation(item: MovieDetailsCollectionItem) {
    if (item is MovieItem && item.translation == null) {
      onMissingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    with(binding) {
      badgeImage.invisible()
      placeholderImage.invisible()
      movieImage.visible()
      Glide.with(this@MovieDetailsCollectionItemView).clear(movieImage)
    }
  }
}
