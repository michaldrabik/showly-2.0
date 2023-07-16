package com.michaldrabik.ui_base.common.sheets.links.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.databinding.ViewLinksItemBinding

class LinkItemView : FrameLayout {

  private val binding = ViewLinksItemBinding.inflate(LayoutInflater.from(context), this)

  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    context.theme.obtainStyledAttributes(attrs, R.styleable.LinkItem, 0, 0).apply {
      try {
        with(binding) {
          viewLinkItemName.text = getString(R.styleable.LinkItem_text)
          viewLinkItemImage.setImageResource(getResourceId(R.styleable.LinkItem_icon, -1))
        }
      } finally {
        recycle()
      }
    }
  }
}
