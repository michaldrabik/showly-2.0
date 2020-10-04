package com.michaldrabik.ui_my_shows.myshows.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.MyShowsSection.RECENTS
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import kotlinx.android.synthetic.main.view_my_shows_header.view.*

class MyShowHeaderView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_my_shows_header, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    clipChildren = false
    clipToPadding = false
  }

  fun bind(item: MyShowsItem.Header, sortClickListener: ((MyShowsSection, SortOrder) -> Unit)?) {
    bindLabel(item)

    item.sortOrder?.let {
      myShowsHeaderSortButton.visible()
      myShowsHeaderSortButton.onClick {
        sortClickListener?.invoke(item.section, item.sortOrder)
      }
    }
  }

  private fun bindLabel(item: MyShowsItem.Header) {
    myShowsHeaderLabel.text = when (item.section) {
      RECENTS -> item.section.displayString
      else -> "${item.section.displayString} (${item.itemCount})"
    }
  }
}
