package com.michaldrabik.showly2.ui.followedshows.myshows.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.model.MyShowsSection.RECENTS
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.model.SortOrder.NAME
import com.michaldrabik.showly2.model.SortOrder.NEWEST
import com.michaldrabik.showly2.model.SortOrder.RATING
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsItem
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visible
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

    myShowsHeaderSortView.setAvailable(listOf(NAME, NEWEST, RATING))
    myShowsHeaderSortButton.onClick {
      myShowsHeaderSortView.fadeIn()
    }
  }

  fun bind(item: MyShowsItem.Header, sortSelectListener: ((MyShowsSection, SortOrder) -> Unit)?) {
    bindLabel(item)

    item.sortOrder?.let {
      myShowsHeaderSortView.bind(it)
      myShowsHeaderSortButton.visible()
    }

    myShowsHeaderSortView.sortSelectedListener = {
      myShowsHeaderSortView.fadeOut()
      myShowsHeaderSortButton.visible()
      sortSelectListener?.invoke(item.section, it)
    }
  }

  private fun bindLabel(item: MyShowsItem.Header) {
    myShowsHeaderLabel.text = when (item.section) {
      RECENTS -> item.section.displayString
      else -> "${item.section.displayString} (${item.itemCount})"
    }
  }
}
