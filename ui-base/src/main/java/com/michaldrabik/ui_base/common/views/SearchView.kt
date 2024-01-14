package com.michaldrabik.ui_base.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.behaviour.SearchViewBehaviour
import com.michaldrabik.ui_base.databinding.ViewSearchBinding
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.expandTouch
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf

class SearchView : FrameLayout, CoordinatorLayout.AttachedBehavior {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  val binding = ViewSearchBinding.inflate(LayoutInflater.from(context), this, true)

  var onSettingsClickListener: (() -> Unit)? = null
  var onStatsClickListener: (() -> Unit)? = null
  var onTraktClickListener: (() -> Unit)? = null

  init {
    with(binding) {
      searchSettingsIcon.expandTouch()
      searchSettingsIcon.onClick { onSettingsClickListener?.invoke() }
      searchStatsIcon.onClick { onStatsClickListener?.invoke() }
      searchTraktIcon.onClick { onTraktClickListener?.invoke() }
    }
  }

  var hint: String
    get() = binding.searchViewInput.hint.toString()
    set(value) {
      with(binding) {
        searchViewInput.hint = value
        searchViewText.text = value
      }
    }

  var settingsIconVisible
    get() = binding.searchSettingsIcon.isVisible
    set(value) {
      binding.searchSettingsIcon.visibleIf(value)
    }

  var statsIconVisible
    get() = binding.searchStatsIcon.isVisible
    set(value) {
      binding.searchStatsIcon.visibleIf(value)
    }

  var traktIconVisible
    get() = binding.searchTraktIcon.isVisible
    set(value) {
      binding.searchTraktIcon.visibleIf(value)
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
    binding.searchViewInput.isEnabled = enabled
    super.setEnabled(enabled)
  }

  fun setTraktProgress(isProgress: Boolean, withIcon: Boolean = false) {
    with(binding) {
      searchViewIcon.visibleIf(!isProgress)
      searchViewText.visibleIf(!isProgress)
      searchTraktIcon.visibleIf(!isProgress && withIcon)
      searchViewTraktSync.visibleIf(isProgress)
    }
  }
}
