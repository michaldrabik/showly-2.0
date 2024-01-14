package com.michaldrabik.ui_base.common.views

import android.content.Context
import android.util.AttributeSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.tabs.TabLayout
import com.michaldrabik.ui_base.common.behaviour.ScrollableViewBehaviour

class ScrollableTabLayout : TabLayout, CoordinatorLayout.AttachedBehavior {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  override fun getBehavior() = ScrollableViewBehaviour()
}
