package com.michaldrabik.ui_progress_movies.progress.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.SortType.ASCENDING
import com.michaldrabik.ui_model.SortType.DESCENDING
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.databinding.ViewProgressMoviesFiltersBinding

class ProgressMoviesFiltersView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewProgressMoviesFiltersBinding.inflate(LayoutInflater.from(context), this)

  var onSortChipClicked: ((SortOrder, SortType) -> Unit)? = null

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  fun bind(
    sortOrder: SortOrder,
    sortType: SortType,
  ) {
    with(binding) {
      val sortIcon = when (sortType) {
        ASCENDING -> R.drawable.ic_arrow_alt_up
        DESCENDING -> R.drawable.ic_arrow_alt_down
      }
      progressFiltersSortingChip.closeIcon = ContextCompat.getDrawable(context, sortIcon)
      progressFiltersSortingChip.text = context.getText(sortOrder.displayString)
      progressFiltersSortingChip.onClick {
        onSortChipClicked?.invoke(sortOrder, sortType)
      }
    }
  }
}
