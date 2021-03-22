package com.michaldrabik.ui_lists.details.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import kotlinx.android.synthetic.main.view_list_details_movie_item.view.*
import java.util.Locale.ENGLISH

class ListDetailsMovieItemView : ListDetailsItemView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_list_details_movie_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)

    imageLoadCompleteListener = {
      if (item.translation == null) {
        missingTranslationListener?.invoke(item)
      }
    }

    listDetailsMovieRoot.onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = listDetailsMovieImage
  override val placeholderView: ImageView = listDetailsMoviePlaceholder

  override fun bind(item: ListDetailsItem) {
    super.bind(item)
    Glide.with(this).clear(listDetailsMovieImage)
    val movie = item.requireMovie()

    listDetailsMovieProgress.visibleIf(item.isLoading)

    listDetailsMovieTitle.text =
      if (item.translation?.title.isNullOrBlank()) movie.title
      else item.translation?.title?.capitalizeWords()

    listDetailsMovieDescription.text =
      when {
        item.translation?.overview.isNullOrBlank() -> {
          if (movie.overview.isNotBlank()) movie.overview
          else context.getString(R.string.textNoDescription)
        }
        else -> item.translation?.overview
      }

    listDetailsMovieHeader.text = String.format(ENGLISH, "%d", movie.year)
    listDetailsMovieRating.text = String.format(ENGLISH, "%.1f", movie.rating)
    loadImage(item)
  }
}
