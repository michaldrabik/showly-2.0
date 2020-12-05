package com.michaldrabik.ui_my_movies.mymovies.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.MovieView
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem
import kotlinx.android.synthetic.main.view_my_movies_all.view.*
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class MyMovieAllView : MovieView<MyMoviesItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_my_movies_all, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    myMovieAllRoot.onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = myMovieAllImage
  override val placeholderView: ImageView = myMovieAllPlaceholder

  private lateinit var item: MyMoviesItem

  override fun bind(
    item: MyMoviesItem,
    missingImageListener: ((MyMoviesItem, Boolean) -> Unit)?
  ) {
    clear()
    this.item = item
    myMovieAllProgress.visibleIf(item.isLoading)
    myMovieAllTitle.text =
      if (item.translation?.title.isNullOrBlank()) item.movie.title
      else item.translation?.title?.capitalizeWords()

    myMovieAllDescription.text =
      if (item.translation?.overview.isNullOrBlank()) item.movie.overview
      else item.translation?.overview

    myMovieAllYear.text = String.format(ENGLISH, "%d", item.movie.year)
    myMovieAllRating.text = String.format(ENGLISH, "%.1f", item.movie.rating)
    myMovieAllDescription.visibleIf(item.movie.overview.isNotBlank())
    myMovieAllYear.visibleIf(item.movie.year > 0)

    loadImage(item, missingImageListener)
  }

  private fun clear() {
    myMovieAllTitle.text = ""
    myMovieAllDescription.text = ""
    myMovieAllYear.text = ""
    myMovieAllRating.text = ""
    myMovieAllPlaceholder.gone()
    Glide.with(this).clear(myMovieAllImage)
  }
}
