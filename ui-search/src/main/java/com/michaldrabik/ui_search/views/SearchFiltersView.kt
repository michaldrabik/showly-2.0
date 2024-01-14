package com.michaldrabik.ui_search.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.common.behaviour.ScrollableViewBehaviour
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_search.R
import com.michaldrabik.ui_search.databinding.ViewSearchFiltersBinding

class SearchFiltersView : FrameLayout, CoordinatorLayout.AttachedBehavior {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewSearchFiltersBinding.inflate(LayoutInflater.from(context), this, true)

  init {
    binding.viewSearchFiltersShowsChip.setOnCheckedChangeListener { _, _ -> onChipCheckChange() }
    binding.viewSearchFiltersMoviesChip.setOnCheckedChangeListener { _, _ -> onChipCheckChange() }
  }

  var onChipsChangeListener: ((List<Mode>) -> Unit)? = null
  var onSortClickListener: ((SortOrder, SortType) -> Unit)? = null

  var isListenerEnabled = true

  private fun onChipCheckChange() {
    with(binding) {
      val ids = viewSearchFiltersChipGroup.checkedChipIds
        .filterNot { it == viewSearchFiltersSortChip.id }
        .map {
          when (it) {
            viewSearchFiltersShowsChip.id -> Mode.SHOWS
            viewSearchFiltersMoviesChip.id -> Mode.MOVIES
            else -> throw IllegalStateException()
          }
        }
      onChipsChangeListener?.invoke(ids)
    }
  }

  override fun setEnabled(enabled: Boolean) {
    binding.viewSearchFiltersChipGroup.forEach {
      it.isEnabled = enabled
    }
  }

  fun setSorting(sortOrder: SortOrder, sortType: SortType) {
    with(binding) {
      viewSearchFiltersSortChip.text = context.getString(sortOrder.displayString)
      viewSearchFiltersSortChip.onClick {
        onSortClickListener?.invoke(sortOrder, sortType)
      }
      val sortIcon = when (sortType) {
        SortType.ASCENDING -> R.drawable.ic_arrow_alt_up
        SortType.DESCENDING -> R.drawable.ic_arrow_alt_down
      }
      viewSearchFiltersSortChip.closeIcon = ContextCompat.getDrawable(context, sortIcon)
    }
  }

  fun setTypes(types: List<Mode>) {
    isListenerEnabled = false
    binding.viewSearchFiltersShowsChip.isChecked = Mode.SHOWS in types
    binding.viewSearchFiltersMoviesChip.isChecked = Mode.MOVIES in types
    isListenerEnabled = true
  }

  fun setEnabledTypes(types: List<Mode>) {
    val hasShows = types.contains(Mode.SHOWS)
    val hasMovies = types.contains(Mode.MOVIES)
    with(binding) {
      viewSearchFiltersShowsChip.isEnabled = hasShows
      viewSearchFiltersShowsChip.visibleIf(hasShows)
      viewSearchFiltersMoviesChip.isEnabled = hasMovies
      viewSearchFiltersMoviesChip.visibleIf(hasMovies)
    }
  }

  override fun getBehavior() = ScrollableViewBehaviour()
}
