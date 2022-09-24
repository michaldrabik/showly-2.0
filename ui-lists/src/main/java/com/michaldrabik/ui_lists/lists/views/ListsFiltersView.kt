package com.michaldrabik.ui_lists.lists.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.databinding.ViewListsFiltersBinding
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType

class ListsFiltersView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewListsFiltersBinding.inflate(LayoutInflater.from(context), this)

  var onSortClickListener: ((SortOrder, SortType) -> Unit)? = null

  override fun setEnabled(enabled: Boolean) {
    binding.viewListsFiltersChipGroup.forEach {
      it.isEnabled = enabled
    }
  }

  fun setSorting(sortOrder: SortOrder, sortType: SortType) {
    with(binding) {
      viewListsFilterSortChip.text = context.getString(sortOrder.displayString)
      viewListsFilterSortChip.onClick {
        onSortClickListener?.invoke(sortOrder, sortType)
      }
      val sortIcon = when (sortType) {
        SortType.ASCENDING -> R.drawable.ic_arrow_alt_up
        SortType.DESCENDING -> R.drawable.ic_arrow_alt_down
      }
      viewListsFilterSortChip.closeIcon = ContextCompat.getDrawable(context, sortIcon)
    }
  }
}
