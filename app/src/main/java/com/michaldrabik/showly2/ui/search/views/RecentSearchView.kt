package com.michaldrabik.showly2.ui.search.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.ui_model.RecentSearch
import kotlinx.android.synthetic.main.view_search_recent.view.*

class RecentSearchView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_search_recent, this)
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
  }

  fun bind(item: RecentSearch) {
    searchRecentText.text = item.text
  }
}
