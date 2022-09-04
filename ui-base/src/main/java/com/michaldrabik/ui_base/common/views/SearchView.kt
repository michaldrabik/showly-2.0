package com.michaldrabik.ui_base.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.behaviour.SearchViewBehaviour
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.expandTouch
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.view_search.*
import kotlinx.android.synthetic.main.view_search.view.*

class SearchView : FrameLayout, CoordinatorLayout.AttachedBehavior {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onSettingsClickListener: (() -> Unit)? = null
  var onSortClickListener: (() -> Unit)? = null
  var onStatsClickListener: (() -> Unit)? = null
  var onTraktClickListener: (() -> Unit)? = null

  init {
    inflate(context, R.layout.view_search, this)

    searchSortIcon.expandTouch()
    searchSettingsIcon.expandTouch()
    searchSortIcon.onClick { onSortClickListener?.invoke() }
    searchSettingsIcon.onClick { onSettingsClickListener?.invoke() }
    searchStatsIcon.onClick { onStatsClickListener?.invoke() }
    searchTraktIcon.onClick { onTraktClickListener?.invoke() }
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

  var statsIconVisible
    get() = searchStatsIcon.isVisible
    set(value) {
      searchStatsIcon.visibleIf(value)
    }

  var traktIconVisible
    get() = searchTraktIcon.isVisible
    set(value) {
      searchTraktIcon.visibleIf(value)
    }

  var sortIconClickable
    get() = searchSortIcon.isClickable
    set(value) {
      searchSortIcon.isClickable = value
    }

  var iconBadgeVisible
    get() = searchDotBadge.isVisible
    set(value) {
      searchDotBadge.visibleIf(value)
    }

  var isSearching = false

  override fun onAttachedToWindow() {
    doOnApplyWindowInsets { _, insets, _, _ ->
      val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      applyWindowInsetBehaviour(context.dimenToPx(R.dimen.spaceNormal) + inset)
    }
    super.onAttachedToWindow()
  }

  fun applyWindowInsetBehaviour(newInset: Int) {
    updateLayoutParams {
      (layoutParams as? CoordinatorLayout.LayoutParams)?.behavior = SearchViewBehaviour(newInset)
    }
  }

  override fun getBehavior() = SearchViewBehaviour(context.dimenToPx(R.dimen.spaceNormal))

  override fun setEnabled(enabled: Boolean) {
    searchViewInput.isEnabled = enabled
    super.setEnabled(enabled)
  }

  fun setTraktProgress(isProgress: Boolean, withIcon: Boolean = false) {
    searchViewIcon.visibleIf(!isProgress)
    searchViewText.visibleIf(!isProgress)
    searchTraktIcon.visibleIf(!isProgress && withIcon)
    searchViewTraktSync.visibleIf(isProgress)
  }
}

inline val Fragment.exSearchViewInput: TextInputEditText get() = searchViewInput
inline val Fragment.exSearchViewText: TextView get() = searchViewText
inline val Fragment.exSearchViewIcon: ImageView get() = searchViewIcon
