package com.michaldrabik.ui_my_movies.mymovies.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.MyMoviesSection.RECENTS
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_my_movies.databinding.ViewMyMoviesHeaderBinding
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem
import java.util.Locale.ENGLISH

class MyMovieHeaderView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewMyMoviesHeaderBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    clipChildren = false
    clipToPadding = false
  }

  fun bind(
    item: MyMoviesItem.Header,
    sortClickListener: (SortOrder, SortType) -> Unit,
  ) {
    bindLabel(item)
    with(binding) {
      myMoviesHeaderSortButton.visibleIf(item.sortOrder != null)
      item.sortOrder?.let { sortOrder ->
        myMoviesHeaderSortButton.text = context.getString(sortOrder.first.displayString)
        myMoviesHeaderSortButton.onClick { sortClickListener(sortOrder.first, sortOrder.second) }
        val sortIcon = when (sortOrder.second) {
          SortType.ASCENDING -> R.drawable.ic_arrow_alt_up
          SortType.DESCENDING -> R.drawable.ic_arrow_alt_down
        }
        myMoviesHeaderSortButton.closeIcon = ContextCompat.getDrawable(context, sortIcon)
      }
    }
  }

  private fun bindLabel(item: MyMoviesItem.Header) {
    val headerLabel = context.getString(item.section.displayString)
    binding.myMoviesHeaderLabel.text = when (item.section) {
      RECENTS -> headerLabel
      else -> String.format(ENGLISH, "%s (%d)", headerLabel, item.itemCount)
    }
  }
}
