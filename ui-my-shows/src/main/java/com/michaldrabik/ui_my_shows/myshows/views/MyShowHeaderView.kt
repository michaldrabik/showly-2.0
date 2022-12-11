package com.michaldrabik.ui_my_shows.myshows.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.MyShowsSection.ALL
import com.michaldrabik.ui_model.MyShowsSection.RECENTS
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.databinding.ViewMyShowsHeaderBinding
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import java.util.Locale.ENGLISH

class MyShowHeaderView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewMyShowsHeaderBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    clipChildren = false
    clipToPadding = false
  }

  fun bind(
    item: MyShowsItem.Header,
    sortClickListener: ((MyShowsSection, SortOrder, SortType) -> Unit)?,
  ) {
    bindLabel(item)
    with(binding) {
      myShowsFilterChipsScroll.visibleIf(item.section == ALL)
      myShowsSortChip.visibleIf(item.sortOrder != null)

      with(myShowsTypeChip) {
        visibleIf(item.section != RECENTS)
        isSelected = true
        text = context.getString(item.section.displayString)
      }

      item.sortOrder?.let { sortOrder ->
        myShowsSortChip.text = context.getString(sortOrder.first.displayString)
        myShowsSortChip.onClick {
          sortClickListener?.invoke(item.section, sortOrder.first, sortOrder.second)
        }
        val sortIcon = when (sortOrder.second) {
          SortType.ASCENDING -> R.drawable.ic_arrow_alt_up
          SortType.DESCENDING -> R.drawable.ic_arrow_alt_down
        }
        myShowsSortChip.closeIcon = ContextCompat.getDrawable(context, sortIcon)
      }
    }
  }

  private fun bindLabel(item: MyShowsItem.Header) {
    val headerLabel = context.getString(item.section.displayString)
    binding.myShowsHeaderLabel.text = when (item.section) {
      RECENTS -> headerLabel
      else -> String.format(ENGLISH, "%s (%d)", headerLabel, item.itemCount)
    }
  }
}
