package com.michaldrabik.ui_base.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.michaldrabik.ui_base.common.behaviour.ScrollableViewBehaviour
import com.michaldrabik.ui_base.databinding.ViewSearchLocalBinding
import com.michaldrabik.ui_base.utilities.extensions.onClick

class SearchLocalView : FrameLayout, CoordinatorLayout.AttachedBehavior {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  val binding = ViewSearchLocalBinding.inflate(LayoutInflater.from(context), this, true)

  var onCloseClickListener: (() -> Unit)? = null

  init {
    binding.searchViewLocalIcon.onClick { onCloseClickListener?.invoke() }
  }

  override fun getBehavior() = ScrollableViewBehaviour()

  override fun setEnabled(enabled: Boolean) {
    binding.searchViewLocalInput.isEnabled = enabled
  }
}
