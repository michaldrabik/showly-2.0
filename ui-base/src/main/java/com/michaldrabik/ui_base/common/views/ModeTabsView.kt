package com.michaldrabik.ui_base.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import com.michaldrabik.common.Mode
import com.michaldrabik.common.Mode.MOVIES
import com.michaldrabik.common.Mode.SHOWS
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.databinding.ViewModeTabsBinding
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf

class ModeTabsView : LinearLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewModeTabsBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    orientation = HORIZONTAL

    with(binding) {
      viewMovies.onClick { onModeSelected?.invoke(MOVIES) }
      viewShows.onClick { onModeSelected?.invoke(SHOWS) }
      viewLists.onClick { onListsSelected?.invoke() }
    }
  }

  var onModeSelected: ((Mode) -> Unit)? = null
  var onListsSelected: (() -> Unit)? = null

  fun selectShows() {
    with(binding) {
      viewShows.setTextColor(context.colorFromAttr(R.attr.textColorTabSelected))
      viewMovies.setTextColor(context.colorFromAttr(R.attr.textColorTab))
      viewLists.setTextColor(context.colorFromAttr(R.attr.textColorTab))
    }
  }

  fun selectMovies() {
    with(binding) {
      viewShows.setTextColor(context.colorFromAttr(R.attr.textColorTab))
      viewMovies.setTextColor(context.colorFromAttr(R.attr.textColorTabSelected))
      viewLists.setTextColor(context.colorFromAttr(R.attr.textColorTab))
    }
  }

  fun selectLists() {
    with(binding) {
      viewShows.setTextColor(context.colorFromAttr(R.attr.textColorTab))
      viewMovies.setTextColor(context.colorFromAttr(R.attr.textColorTab))
      viewLists.setTextColor(context.colorFromAttr(R.attr.textColorTabSelected))
    }
  }

  fun showMovies(show: Boolean) = binding.viewMovies.visibleIf(show)

  fun showLists(show: Boolean, anchorEnd: Boolean = true) {
    with(binding) {
      viewLists.visibleIf(show)
      viewSpacer.visibleIf(anchorEnd)
    }
  }

  override fun setEnabled(enabled: Boolean) {
    with(binding) {
      viewShows.isEnabled = enabled
      viewMovies.isEnabled = enabled
      viewLists.isEnabled = enabled
    }
  }
}
