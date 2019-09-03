package com.michaldrabik.showly2.ui.common.views

import android.content.Context
import android.util.AttributeSet
import com.michaldrabik.showly2.R

class ShowSearchView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ShowView(context, attrs, defStyleAttr) {

  init {
    inflate(context, R.layout.view_show_search, this)
    clipChildren = false
    clipToPadding = false
  }

}