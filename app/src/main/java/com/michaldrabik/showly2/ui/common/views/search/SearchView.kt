package com.michaldrabik.showly2.ui.common.views.search

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.common.behaviour.SearchViewBehaviour
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.showly2.utilities.extensions.expandTouch
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.view_search.view.*

class SearchView : FrameLayout, CoordinatorLayout.AttachedBehavior {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onSettingsClickListener: (() -> Unit)? = null
  var onSortClickListener: (() -> Unit)? = null

  init {
    inflate(context, R.layout.view_search, this)

    searchSortIcon.expandTouch()
    searchSettingsIcon.expandTouch()
    searchSortIcon.onClick { onSortClickListener?.invoke() }
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

  var sortIconVisible
    get() = searchSortIcon.isVisible
    set(value) {
      searchSortIcon.visibleIf(value)
    }

  var isSearching = false

  override fun onAttachedToWindow() {
    doOnApplyWindowInsets { _, insets, _, _ ->
      applyWindowInsetBehaviour(context.dimenToPx(R.dimen.spaceNormal) + insets.systemWindowInsetTop)
    }
    super.onAttachedToWindow()
  }

  fun applyWindowInsetBehaviour(newInset: Int) {
    updateLayoutParams {
      (layoutParams as? CoordinatorLayout.LayoutParams)?.behavior = SearchViewBehaviour(newInset)
    }
  }

  override fun getBehavior() =
    SearchViewBehaviour(context.dimenToPx(R.dimen.spaceNormal))

  override fun setEnabled(enabled: Boolean) {
    searchViewInput.isEnabled = enabled
  }

  override fun setClickable(clickable: Boolean) {
    searchSettingsIcon.isClickable = clickable
    searchSortIcon.isClickable = clickable
    super.setClickable(clickable)
  }
}
