package com.michaldrabik.ui_lists.details.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_lists.R
import kotlinx.android.synthetic.main.view_list_filters.view.*

class ListDetailsFiltersView : LinearLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onChipsChangeListener: ((List<Mode>) -> Unit)? = null

  init {
    inflate(context, R.layout.view_list_filters, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    orientation = VERTICAL

    viewListFiltersShowsChip.setOnCheckedChangeListener { _, _ -> onChipCheckChange() }
    viewListFiltersMoviesChip.setOnCheckedChangeListener { _, _ -> onChipCheckChange() }
  }

  private fun onChipCheckChange() {
    val ids = viewListFiltersChipGroup.checkedChipIds.map {
      when (it) {
        viewListFiltersShowsChip.id -> Mode.SHOWS
        viewListFiltersMoviesChip.id -> Mode.MOVIES
        else -> throw IllegalStateException()
      }
    }
    if (ids.isNotEmpty()) {
      onChipsChangeListener?.invoke(ids)
    }
  }

  fun setTypes(types: List<Mode>) {
    viewListFiltersShowsChip.isChecked = Mode.SHOWS in types
    viewListFiltersMoviesChip.isChecked = Mode.MOVIES in types
  }
}
