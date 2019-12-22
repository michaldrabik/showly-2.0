package com.michaldrabik.showly2.ui.common.views.search

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.common.behaviour.SearchViewBehaviour
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.expandTouchArea
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.view_search.view.*

class SearchView : FrameLayout, CoordinatorLayout.AttachedBehavior {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onSettingsClickListener: (() -> Unit)? = null

  init {
    inflate(context, R.layout.view_search, this)
    searchSettingsIcon.expandTouchArea()
    searchSettingsIcon.onClick { onSettingsClickListener?.invoke() }
  }

  var hint: String
    get() = searchViewInput.hint.toString()
    set(value) {
      searchViewInput.hint = value
      searchViewText.text = value
    }

  var settingsIconVisible
    get() = searchSettingsIcon.isVisible
    set(value) {
      searchSettingsIcon.visibleIf(value)
    }

  var isSearching = false

  override fun getBehavior() =
    SearchViewBehaviour(context.dimenToPx(R.dimen.spaceSmall))

  override fun setEnabled(enabled: Boolean) {
    searchViewInput.isEnabled = enabled
  }
}