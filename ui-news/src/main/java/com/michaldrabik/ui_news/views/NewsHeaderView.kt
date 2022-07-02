package com.michaldrabik.ui_news.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.michaldrabik.ui_base.common.behaviour.ScrollableViewBehaviour
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_news.databinding.ViewNewsHeaderBinding

class NewsHeaderView : FrameLayout, CoordinatorLayout.AttachedBehavior {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onSettingsClickListener: (() -> Unit)? = null

  private val binding = ViewNewsHeaderBinding.inflate(LayoutInflater.from(context), this)

  init {
    binding.viewNewsHeaderSettingsIcon.onClick { onSettingsClickListener?.invoke() }
  }

  override fun getBehavior() = ScrollableViewBehaviour()
}
