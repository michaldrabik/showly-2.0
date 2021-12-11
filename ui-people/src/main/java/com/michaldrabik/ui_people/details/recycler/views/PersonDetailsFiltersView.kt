package com.michaldrabik.ui_people.details.recycler.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_people.R
import kotlinx.android.synthetic.main.view_person_details_filters.view.*

class PersonDetailsFiltersView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_person_details_filters, this)

    viewPersonDetailsFiltersShowsChip.setOnCheckedChangeListener { _, _ -> onChipCheckChange() }
    viewPersonDetailsFiltersMoviesChip.setOnCheckedChangeListener { _, _ -> onChipCheckChange() }
  }

  var onChipsChangeListener: ((List<Mode>) -> Unit)? = null
  var isListenerEnabled = true

  private fun onChipCheckChange() {
    if (!isListenerEnabled) return
    val ids = viewPersonDetailsFiltersChipGroup.checkedChipIds.map {
      when (it) {
        viewPersonDetailsFiltersShowsChip.id -> Mode.SHOWS
        viewPersonDetailsFiltersMoviesChip.id -> Mode.MOVIES
        else -> throw IllegalStateException()
      }
    }
    onChipsChangeListener?.invoke(ids)
  }

  override fun setEnabled(enabled: Boolean) {
    viewPersonDetailsFiltersShowsChip.isEnabled = enabled
    viewPersonDetailsFiltersMoviesChip.isEnabled = enabled
  }

  fun bind(types: List<Mode>) {
    isListenerEnabled = false
    viewPersonDetailsFiltersShowsChip.isChecked = Mode.SHOWS in types
    viewPersonDetailsFiltersMoviesChip.isChecked = Mode.MOVIES in types
    isListenerEnabled = true
  }
}
