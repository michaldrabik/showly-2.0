package com.michaldrabik.ui_base.common.sheets.links.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import com.michaldrabik.ui_base.R
import kotlinx.android.synthetic.main.view_links_item.view.*

class LinkItemView : FrameLayout {

  constructor(context: Context) : this(context, null)

  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    inflate(context, R.layout.view_links_item, this)
    layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    context.theme.obtainStyledAttributes(attrs, R.styleable.LinkItem, 0, 0).apply {
      try {
        viewLinkItemName.text = getString(R.styleable.LinkItem_text)
        viewLinkItemImage.setImageResource(getResourceId(R.styleable.LinkItem_icon, -1))
      } finally {
        recycle()
      }
    }
  }
}
