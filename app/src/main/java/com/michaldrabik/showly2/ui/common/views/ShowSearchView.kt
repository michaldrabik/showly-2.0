package com.michaldrabik.showly2.ui.common.views

import android.content.Context
import android.util.AttributeSet
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import com.michaldrabik.showly2.utilities.extensions.onClick
import kotlinx.android.synthetic.main.view_show_search.view.*

class ShowSearchView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ShowView(context, attrs, defStyleAttr) {

  init {
    inflate(context, R.layout.view_show_search, this)
    clipChildren = false
    clipToPadding = false
    searchCardLayout.onClick { searchClickListener.invoke() }
  }

  var searchClickListener: () -> Unit = { }

  override fun bind(
    item: DiscoverListItem,
    missingImageListener: (DiscoverListItem, Boolean) -> Unit,
    itemClickListener: (DiscoverListItem) -> Unit
  ) {
    //Do nothing. Holds super.bind() size calculations.
  }
}