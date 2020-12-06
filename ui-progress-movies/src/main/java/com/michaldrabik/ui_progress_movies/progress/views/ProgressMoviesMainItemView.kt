package com.michaldrabik.ui_progress_movies.progress.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.addRipple
import com.michaldrabik.ui_base.utilities.extensions.bump
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.expandTouch
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_progress_movies.ProgressMovieItem
import com.michaldrabik.ui_progress_movies.R
import kotlinx.android.synthetic.main.view_progress_movies_main_item.view.*

@SuppressLint("SetTextI18n")
class ProgressMoviesMainItemView : MovieView<ProgressMovieItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var itemLongClickListener: ((ProgressMovieItem, View) -> Unit)? = null
  var checkClickListener: ((ProgressMovieItem) -> Unit)? = null
  var missingImageListener: ((ProgressMovieItem, Boolean) -> Unit)? = null

  init {
    inflate(context, R.layout.view_progress_movies_main_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    addRipple()
    progressMovieItemCheckButton.expandTouch(100)

    onClick { itemClickListener?.invoke(item) }
    setOnLongClickListener {
      itemLongClickListener?.invoke(item, progressMovieItemTitle)
      true
    }
  }

  private lateinit var item: ProgressMovieItem

  override val imageView: ImageView = progressMovieItemImage
  override val placeholderView: ImageView = progressMovieItemPlaceholder

  fun bind(item: ProgressMovieItem) {
    this.item = item
    clear()

    val translationTitle = item.movieTranslation?.title
    progressMovieItemTitle.text =
      if (translationTitle.isNullOrBlank()) item.movie.title
      else translationTitle.capitalizeWords()

    val translationOverview = item.movieTranslation?.overview
    progressMovieItemSubtitle.text =
      when {
        translationOverview.isNullOrBlank() -> {
          if (item.movie.overview.isBlank()) context.getString(R.string.textNoDescription)
          else item.movie.overview
        }
        else -> translationOverview.capitalizeWords()
      }

    progressMovieItemPin.visibleIf(item.isPinned)

    progressMovieItemCheckButton.onClick {
      it.bump { checkClickListener?.invoke(item) }
    }

    loadImage(item, missingImageListener!!)
  }

  private fun clear() {
    progressMovieItemTitle.text = ""
    progressMovieItemSubtitle.text = ""
    progressMovieItemPlaceholder.gone()
    Glide.with(this).clear(progressMovieItemImage)
  }
}
