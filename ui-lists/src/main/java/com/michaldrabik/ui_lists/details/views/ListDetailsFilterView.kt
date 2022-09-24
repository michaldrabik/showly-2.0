package com.michaldrabik.ui_lists.details.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.databinding.ViewListDetailsFiltersBinding
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType

class ListDetailsFilterView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewListDetailsFiltersBinding.inflate(LayoutInflater.from(context), this)

  var onSortClickListener: ((SortOrder, SortType) -> Unit)? = null
  var onTypesChangeListener: ((List<Mode>) -> Unit)? = null

  init {
    with(binding) {
      showsChip.onClick {
        showsChip.isSelected = !showsChip.isSelected
        onTypeClick()
      }
      moviesChip.onClick {
        moviesChip.isSelected = !moviesChip.isSelected
        onTypeClick()
      }
    }
  }

  override fun setEnabled(enabled: Boolean) {
    binding.chipsGroup.forEach {
      it.isEnabled = enabled
    }
  }

  fun setFilters(
    types: List<Mode>,
    sortOrder: SortOrder,
    sortType: SortType,
  ) {
    with(binding) {
      showsChip.isSelected = Mode.SHOWS in types
      moviesChip.isSelected = Mode.MOVIES in types
      sortingChip.text = context.getString(sortOrder.displayString)
      sortingChip.onClick {
        onSortClickListener?.invoke(sortOrder, sortType)
      }
      val sortIcon = when (sortType) {
        SortType.ASCENDING -> R.drawable.ic_arrow_alt_up
        SortType.DESCENDING -> R.drawable.ic_arrow_alt_down
      }
      sortingChip.closeIcon = ContextCompat.getDrawable(context, sortIcon)
    }
  }

  private fun onTypeClick() {
    val types = mutableListOf<Mode>().apply {
      if (binding.showsChip.isSelected) add(Mode.SHOWS)
      if (binding.moviesChip.isSelected) add(Mode.MOVIES)
    }
    onTypesChangeListener?.invoke(types)
  }
}
