package com.michaldrabik.ui_my_movies.mymovies.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_model.MyMoviesSection
import com.michaldrabik.ui_model.MyMoviesSection.RECENTS
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem
import kotlinx.android.synthetic.main.view_my_movies_header.view.*
import java.util.Locale.ENGLISH

class MyMovieHeaderView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_my_movies_header, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    clipChildren = false
    clipToPadding = false
  }

  fun bind(item: MyMoviesItem.Header, sortClickListener: (MyMoviesSection, SortOrder) -> Unit) {
    bindLabel(item)

    item.sortOrder?.let {
      myMoviesHeaderSortButton.visible()
      myMoviesHeaderSortButton.onClick {
        sortClickListener(item.section, item.sortOrder)
      }
    }
  }

  private fun bindLabel(item: MyMoviesItem.Header) {
    val headerLabel = context.getString(item.section.displayString)
    myMoviesHeaderLabel.text = when (item.section) {
      RECENTS -> headerLabel
      else -> String.format(ENGLISH, "%s (%d)", headerLabel, item.itemCount)
    }
  }
}
