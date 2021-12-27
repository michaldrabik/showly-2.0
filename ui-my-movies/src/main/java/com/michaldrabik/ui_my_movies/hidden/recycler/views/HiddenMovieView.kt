package com.michaldrabik.ui_my_movies.hidden.recycler.views

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
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_my_movies.hidden.recycler.HiddenListItem
import kotlinx.android.synthetic.main.view_hidden_show.view.*
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class HiddenMovieView : MovieView<HiddenListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_hidden_show, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    hiddenMovieRoot.onClick { itemClickListener?.invoke(item) }
    hiddenMovieRoot.onLongClick { itemLongClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = hiddenMovieImage
  override val placeholderView: ImageView = hiddenMoviePlaceholder

  private lateinit var item: HiddenListItem

  override fun bind(item: HiddenListItem) {
    clear()
    this.item = item
    hiddenMovieProgress.visibleIf(item.isLoading)
    hiddenMovieTitle.text =
      if (item.translation?.title.isNullOrBlank()) item.movie.title
      else item.translation?.title

    hiddenMovieDescription.text =
      when {
        item.translation?.overview.isNullOrBlank() -> {
          if (item.movie.overview.isNotBlank()) item.movie.overview
          else context.getString(R.string.textNoDescription)
        }
        else -> item.translation?.overview
      }

    hiddenMovieRating.text = String.format(ENGLISH, "%.1f", item.movie.rating)
    hiddenMovieDescription.visibleIf(item.movie.overview.isNotBlank())
    hiddenMovieNetwork.visibleIf(!item.movie.hasNoDate())
    hiddenMovieNetwork.text = when {
      item.movie.released != null -> item.dateFormat?.format(item.movie.released)?.capitalizeWords()
      else -> String.format(ENGLISH, "%d", item.movie.year)
    }

    item.userRating?.let {
      hiddenMovieUserStarIcon.visible()
      hiddenMovieUserRating.visible()
      hiddenMovieUserRating.text = String.format(ENGLISH, "%d", it)
    }

    loadImage(item)
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    hiddenMovieTitle.text = ""
    hiddenMovieDescription.text = ""
    hiddenMovieNetwork.text = ""
    hiddenMovieRating.text = ""
    hiddenMoviePlaceholder.gone()
    hiddenMovieUserRating.gone()
    hiddenMovieUserStarIcon.gone()
    Glide.with(this).clear(hiddenMovieImage)
  }
}
