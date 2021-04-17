package com.michaldrabik.ui_news.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.michaldrabik.ui_base.common.behaviour.ScrollableViewBehaviour
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_news.R
import kotlinx.android.synthetic.main.view_news_header.view.*

class NewsHeaderView : FrameLayout, CoordinatorLayout.AttachedBehavior {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onSettingsClickListener: (() -> Unit)? = null

  init {
    inflate(context, R.layout.view_news_header, this)
    viewNewsHeaderSettingsIcon.onClick { onSettingsClickListener?.invoke() }
  }

  override fun getBehavior() = ScrollableViewBehaviour()
}
