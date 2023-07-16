package com.michaldrabik.ui_progress_movies.progress.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.common.Config.SPOILERS_HIDE_SYMBOL
import com.michaldrabik.common.Config.SPOILERS_RATINGS_HIDE_SYMBOL
import com.michaldrabik.common.Config.SPOILERS_REGEX
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.addRipple
import com.michaldrabik.ui_base.utilities.extensions.bump
import com.michaldrabik.ui_base.utilities.extensions.colorStateListFromAttr
import com.michaldrabik.ui_base.utilities.extensions.expandTouch
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.SortOrder.USER_RATING
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.databinding.ViewProgressMoviesMainItemBinding
import com.michaldrabik.ui_progress_movies.progress.recycler.ProgressMovieListItem
import java.util.Locale

@SuppressLint("SetTextI18n")
class ProgressMoviesItemView : MovieView<ProgressMovieListItem.MovieItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewProgressMoviesMainItemBinding.inflate(LayoutInflater.from(context), this)

  var checkClickListener: ((ProgressMovieListItem.MovieItem) -> Unit)? = null

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    addRipple()
    binding.progressMovieItemCheckButton.expandTouch(100)
    onClick { itemClickListener?.invoke(item) }
    onLongClick { itemLongClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  private lateinit var item: ProgressMovieListItem.MovieItem

  override val imageView: ImageView = binding.progressMovieItemImage
  override val placeholderView: ImageView = binding.progressMovieItemPlaceholder

  override fun bind(item: ProgressMovieListItem.MovieItem) {
    this.item = item
    clear()

    with(binding) {
      val translationTitle = item.translation?.title
      progressMovieItemTitle.text =
        if (translationTitle.isNullOrBlank()) item.movie.title
        else translationTitle

      bindDescription(item)
      bindRating(item)

      progressMovieItemPin.visibleIf(item.isPinned)
      progressMovieItemCheckButton.onClick {
        it.bump { checkClickListener?.invoke(item) }
      }

      loadImage(item)
    }
  }

  private fun bindDescription(item: ProgressMovieListItem.MovieItem) {
    var description = if (item.translation?.overview.isNullOrBlank()) {
      item.movie.overview.ifBlank { context.getString(R.string.textNoDescription) }
    } else {
      item.translation?.overview
    }

    with(binding) {
      if (item.spoilers.isWatchlistMoviesHidden) {
        progressMovieItemSubtitle.tag = description
        description = SPOILERS_REGEX.replace(description.toString(), SPOILERS_HIDE_SYMBOL)

        if (item.spoilers.isTapToReveal) {
          progressMovieItemSubtitle.onClick { view ->
            view.tag?.let { progressMovieItemSubtitle.text = it.toString() }
            view.isClickable = false
          }
        }
      }

      progressMovieItemSubtitle.text = description
    }
  }

  private fun bindRating(item: ProgressMovieListItem.MovieItem) {
    with(binding) {
      when (item.sortOrder) {
        RATING -> {
          progressMovieItemRating.visible()
          progressMovieItemRatingStar.visible()
          progressMovieItemRatingStar.imageTintList = context.colorStateListFromAttr(android.R.attr.colorAccent)
          val rating = String.format(Locale.ENGLISH, "%.1f", item.movie.rating)
          if (item.spoilers.isMyShowsRatingsHidden) {
            progressMovieItemRating.tag = rating
            progressMovieItemRating.text = SPOILERS_RATINGS_HIDE_SYMBOL
            if (item.spoilers.isTapToReveal) {
              progressMovieItemRating.onClick { view ->
                view.tag?.let {
                  progressMovieItemRating.text = it.toString()
                }
                view.isClickable = false
              }
            }
          } else {
            progressMovieItemRating.text = rating
          }
        }
        USER_RATING -> {
          val hasRating = item.userRating != null
          progressMovieItemRating.visibleIf(hasRating)
          progressMovieItemRatingStar.visibleIf(hasRating)
          progressMovieItemRatingStar.imageTintList = context.colorStateListFromAttr(android.R.attr.textColorPrimary)
          progressMovieItemRating.text = String.format(Locale.ENGLISH, "%d", item.userRating)
        }
        else -> {
          progressMovieItemRating.gone()
          progressMovieItemRatingStar.gone()
        }
      }
    }
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    with(binding) {
      progressMovieItemTitle.text = ""
      progressMovieItemSubtitle.text = ""
      progressMovieItemPlaceholder.gone()
      Glide.with(this@ProgressMoviesItemView).clear(progressMovieItemImage)
    }
  }
}
