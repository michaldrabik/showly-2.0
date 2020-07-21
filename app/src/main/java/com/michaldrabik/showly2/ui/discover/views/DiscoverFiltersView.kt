package com.michaldrabik.showly2.ui.discover.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.extensions.dimenToPx

class DiscoverFiltersView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_discover_filters, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    isClickable = true
    clipChildren = false
    clipToPadding = false
    setBackgroundResource(R.drawable.bg_discover_filters)
    setPadding(context.dimenToPx(R.dimen.spaceNormal))
  }
}
