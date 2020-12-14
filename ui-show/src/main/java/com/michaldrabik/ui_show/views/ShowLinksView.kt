package com.michaldrabik.ui_show.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_show.R

class ShowLinksView : LinearLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_links_menu, this)
    layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    orientation = VERTICAL
    setBackgroundColor(context.colorFromAttr(R.attr.colorCardBackground))
  }
}
