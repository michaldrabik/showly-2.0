package com.michaldrabik.ui_base.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.Ratings
import kotlinx.android.synthetic.main.view_ratings_strip.view.*

class RatingsStripView : LinearLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onTraktClick: ((Ratings) -> Unit)? = null
  var onImdbClick: ((Ratings) -> Unit)? = null
  var onMetaClick: ((Ratings) -> Unit)? = null
  var onRottenClick: ((Ratings) -> Unit)? = null

  private val colorPrimary by lazy { context.colorFromAttr(android.R.attr.textColorPrimary) }
  private val colorSecondary by lazy { context.colorFromAttr(android.R.attr.textColorSecondary) }

  private lateinit var ratings: Ratings

  init {
    inflate(context, R.layout.view_ratings_strip, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    orientation = HORIZONTAL
    gravity = Gravity.TOP

    viewRatingsStripTrakt.onClick { onTraktClick?.invoke(ratings) }
    viewRatingsStripImdb.onClick { if (!ratings.isAnyLoading()) onImdbClick?.invoke(ratings) }
    viewRatingsStripMeta.onClick { if (!ratings.isAnyLoading()) onMetaClick?.invoke(ratings) }
    viewRatingsStripRotten.onClick { if (!ratings.isAnyLoading()) onRottenClick?.invoke(ratings) }
  }

  fun bind(ratings: Ratings) {

    fun bindValue(
      ratings: Ratings.Value?,
      valueView: TextView,
      progressView: View,
      linkView: View,
    ) {
      val rating = ratings?.value
      val isLoading = ratings?.isLoading == true
      with(valueView) {
        visibleIf(!isLoading && !rating.isNullOrBlank(), gone = false)
        text = rating
        setTextColor(if (rating != null) colorPrimary else colorSecondary)
      }
      progressView.visibleIf(isLoading)
      linkView.visibleIf(!isLoading && rating.isNullOrBlank())
    }

    this.ratings = ratings
    bindValue(ratings.trakt, viewRatingsStripTraktValue, viewRatingsStripTraktProgress, viewRatingsStripTraktLinkIcon)
    bindValue(ratings.imdb, viewRatingsStripImdbValue, viewRatingsStripImdbProgress, viewRatingsStripImdbLinkIcon)
    bindValue(ratings.metascore, viewRatingsStripMetaValue, viewRatingsStripMetaProgress, viewRatingsStripMetaLinkIcon)
    bindValue(ratings.rottenTomatoes, viewRatingsStripRottenValue, viewRatingsStripRottenProgress, viewRatingsStripRottenLinkIcon)
  }

  fun isBound() = this::ratings.isInitialized && !this.ratings.isAnyLoading()
}
