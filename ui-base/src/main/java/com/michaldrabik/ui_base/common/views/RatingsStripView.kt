package com.michaldrabik.ui_base.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.Ratings
import kotlinx.android.synthetic.main.view_ratings_strip.view.*

class RatingsStripView : LinearLayout {

  companion object {
    private const val EMPTY_SYMBOL = "---"
  }

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onTraktClick: (() -> Unit)? = null
  var onImdbClick: (() -> Unit)? = null
  var onMetaClick: (() -> Unit)? = null
  var onRottenClick: (() -> Unit)? = null

  private val colorPrimary by lazy { context.colorFromAttr(android.R.attr.textColorPrimary) }
  private val colorSecondary by lazy { context.colorFromAttr(android.R.attr.textColorSecondary) }

  init {
    inflate(context, R.layout.view_ratings_strip, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    orientation = HORIZONTAL
    gravity = Gravity.TOP

    viewRatingsStripTrakt.onClick { onTraktClick?.invoke() }
    viewRatingsStripImdb.onClick { onImdbClick?.invoke() }
    viewRatingsStripMeta.onClick { onMetaClick?.invoke() }
    viewRatingsRotten.onClick { onRottenClick?.invoke() }
  }

  fun bind(ratings: Ratings) {

    fun bindTrakt(ratings: Ratings) {
      viewRatingsStripTraktValue.text = ratings.trakt?.value ?: EMPTY_SYMBOL
      viewRatingsStripTraktValue.visibleIf(ratings.trakt?.isLoading == false, gone = false)
      viewRatingsStripTraktProgress.visibleIf(ratings.trakt?.isLoading == true)
      viewRatingsStripTraktValue.setTextColor(if (ratings.trakt?.value != null) colorPrimary else colorSecondary)
    }

    fun bindImdb(ratings: Ratings) {
      viewRatingsStripImdbValue.text = ratings.imdb?.value ?: EMPTY_SYMBOL
      viewRatingsStripImdbValue.visibleIf(ratings.imdb?.isLoading == false, gone = false)
      viewRatingsStripImdbProgress.visibleIf(ratings.imdb?.isLoading == true)
      viewRatingsStripImdbValue.setTextColor(if (ratings.imdb?.value != null) colorPrimary else colorSecondary)
    }

    fun bindMeta(ratings: Ratings) {
      viewRatingsStripMetaValue.text = ratings.metascore?.value ?: EMPTY_SYMBOL
      viewRatingsStripMetaValue.visibleIf(ratings.metascore?.isLoading == false, gone = false)
      viewRatingsStripMetaProgress.visibleIf(ratings.metascore?.isLoading == true)
      viewRatingsStripMetaValue.setTextColor(if (ratings.metascore?.value != null) colorPrimary else colorSecondary)
    }

    fun bindRotten(ratings: Ratings) {
      viewRatingsStripRottenValue.text = ratings.rottenTomatoes?.value ?: EMPTY_SYMBOL
      viewRatingsStripRottenValue.visibleIf(ratings.rottenTomatoes?.isLoading == false, gone = false)
      viewRatingsStripRottenProgress.visibleIf(ratings.rottenTomatoes?.isLoading == true)
      viewRatingsStripRottenValue.setTextColor(if (ratings.rottenTomatoes?.value != null) colorPrimary else colorSecondary)
    }

    bindTrakt(ratings)
    bindImdb(ratings)
    bindMeta(ratings)
    bindRotten(ratings)
  }
}
