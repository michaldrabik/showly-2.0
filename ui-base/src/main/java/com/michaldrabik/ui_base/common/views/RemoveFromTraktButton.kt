package com.michaldrabik.ui_base.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.view_remove_trakt_history_button.view.*

class RemoveFromTraktButton : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onYesClickListener: (() -> Unit)? = null
  var onNoClickListener: (() -> Unit)? = null

  init {
    inflate(context, R.layout.view_remove_trakt_history_button, this)

    viewRemoveTraktHistoryYesButton.onClick { onYesClickListener?.invoke() }
    viewRemoveTraktHistoryNoButton.onClick { onNoClickListener?.invoke() }
  }

  var isLoading: Boolean = false
    set(value) {
      field = value
      viewRemoveTraktHistoryProgress.visibleIf(value)
      viewRemoveTraktHistoryNoButton.visibleIf(!value, gone = false)
      viewRemoveTraktHistoryYesButton.visibleIf(!value, gone = false)
      viewRemoveTraktHistoryText.visibleIf(!value, gone = false)
    }
}
