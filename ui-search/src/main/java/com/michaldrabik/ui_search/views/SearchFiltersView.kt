package com.michaldrabik.ui_search.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.common.behaviour.ScrollableViewBehaviour
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_search.R
import kotlinx.android.synthetic.main.view_search_filters.view.*

class SearchFiltersView : FrameLayout, CoordinatorLayout.AttachedBehavior {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_search_filters, this)

    viewSearchFiltersShowsChip.setOnCheckedChangeListener { _, _ -> onChipCheckChange() }
    viewSearchFiltersMoviesChip.setOnCheckedChangeListener { _, _ -> onChipCheckChange() }
  }

  var onChipsChangeListener: ((List<Mode>) -> Unit)? = null
  var isListenerEnabled = true

  private fun onChipCheckChange() {
    val ids = viewSearchFiltersChipGroup.checkedChipIds.map {
      when (it) {
        viewSearchFiltersShowsChip.id -> Mode.SHOWS
        viewSearchFiltersMoviesChip.id -> Mode.MOVIES
        else -> throw IllegalStateException()
      }
    }
    onChipsChangeListener?.invoke(ids)
  }

  override fun setEnabled(enabled: Boolean) {
    viewSearchFiltersShowsChip.isEnabled = enabled
    viewSearchFiltersMoviesChip.isEnabled = enabled
  }

  fun setTypes(types: List<Mode>) {
    isListenerEnabled = false
    viewSearchFiltersShowsChip.isChecked = Mode.SHOWS in types
    viewSearchFiltersMoviesChip.isChecked = Mode.MOVIES in types
    isListenerEnabled = true
  }

  fun setEnabledTypes(types: List<Mode>) {
    val hasShows = types.contains(Mode.SHOWS)
    val hasMovies = types.contains(Mode.MOVIES)
    viewSearchFiltersShowsChip.isEnabled = hasShows
    viewSearchFiltersShowsChip.visibleIf(hasShows)
    viewSearchFiltersMoviesChip.isEnabled = hasMovies
    viewSearchFiltersMoviesChip.visibleIf(hasMovies)
  }

  override fun getBehavior() = ScrollableViewBehaviour()
}
