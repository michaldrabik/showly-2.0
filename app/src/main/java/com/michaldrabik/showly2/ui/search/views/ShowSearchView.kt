package com.michaldrabik.showly2.ui.search.views

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.search.recycler.SearchListItem

class ShowSearchView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

  init {
    inflate(context, R.layout.view_show_search, this)
  }

  fun bind(
    item: SearchListItem,
    missingImageListener: (SearchListItem, Boolean) -> Unit,
    itemClickListener: (SearchListItem) -> Unit
  ) {

  }
}