package com.michaldrabik.ui_lists.details.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.common.behaviour.ScrollableViewBehaviour
import com.michaldrabik.ui_lists.R
import kotlinx.android.synthetic.main.view_list_details_chips.view.*

class ListDetailsChipsView : FrameLayout, CoordinatorLayout.AttachedBehavior {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_list_details_chips, this)

    viewListDetailsShowsChip.setOnCheckedChangeListener { _, _ -> onChipCheckChange() }
    viewListDetailsMoviesChip.setOnCheckedChangeListener { _, _ -> onChipCheckChange() }
  }

  var onChipsChangeListener: ((List<Mode>) -> Unit)? = null
  var isListenerEnabled = true

  private fun onChipCheckChange() {
    if (!isListenerEnabled) return
    val types = viewListDetailsChipsGroup.checkedChipIds.map {
      when (it) {
        viewListDetailsShowsChip.id -> Mode.SHOWS
        viewListDetailsMoviesChip.id -> Mode.MOVIES
        else -> throw IllegalStateException()
      }
    }
    onChipsChangeListener?.invoke(types)
  }

  override fun setEnabled(enabled: Boolean) {
    viewListDetailsShowsChip.isEnabled = enabled
    viewListDetailsMoviesChip.isEnabled = enabled
  }

  fun setTypes(types: List<Mode>) {
    isListenerEnabled = false
    viewListDetailsShowsChip.isChecked = Mode.SHOWS in types
    viewListDetailsMoviesChip.isChecked = Mode.MOVIES in types
    isListenerEnabled = true
  }

  override fun getBehavior() = ScrollableViewBehaviour()
}
