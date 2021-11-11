package com.michaldrabik.ui_base.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.behaviour.ScrollableViewBehaviour
import com.michaldrabik.ui_base.utilities.extensions.onClick
import kotlinx.android.synthetic.main.view_search_local.*
import kotlinx.android.synthetic.main.view_search_local.view.*

class SearchLocalView : FrameLayout, CoordinatorLayout.AttachedBehavior {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onCloseClickListener: (() -> Unit)? = null

  init {
    inflate(context, R.layout.view_search_local, this)
    searchViewLocalIcon.onClick { onCloseClickListener?.invoke() }
  }

  override fun getBehavior() = ScrollableViewBehaviour()

  override fun setEnabled(enabled: Boolean) {
    searchViewLocalInput.isEnabled = enabled
  }
}

inline val Fragment.exSearchLocalViewInput: TextInputEditText get() = searchViewLocalInput
inline val Fragment.exSearchLocalViewIcon: ImageView get() = searchViewLocalIcon
