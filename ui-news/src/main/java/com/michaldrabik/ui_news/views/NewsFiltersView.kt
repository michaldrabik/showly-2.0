package com.michaldrabik.ui_news.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.michaldrabik.ui_base.common.behaviour.ScrollableViewBehaviour
import com.michaldrabik.ui_model.NewsItem
import com.michaldrabik.ui_news.R
import kotlinx.android.synthetic.main.view_news_filters.view.*

class NewsFiltersView : FrameLayout, CoordinatorLayout.AttachedBehavior {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_news_filters, this)
    setupListeners()
  }

  var onChipsChangeListener: ((List<NewsItem.Type>) -> Unit)? = null

  private fun onChipCheckChange() {
    val ids = viewNewsFiltersChipGroup.checkedChipIds.map {
      when (it) {
        viewNewsFiltersShowsChip.id -> NewsItem.Type.SHOW
        viewNewsFiltersMoviesChip.id -> NewsItem.Type.MOVIE
        else -> throw IllegalStateException()
      }
    }
    onChipsChangeListener?.invoke(ids)
  }

  fun setFilters(filters: List<NewsItem.Type>) {
    clearListeners()

    viewNewsFiltersShowsChip.isChecked = filters.contains(NewsItem.Type.SHOW)
    viewNewsFiltersMoviesChip.isChecked = filters.contains(NewsItem.Type.MOVIE)

    setupListeners()
  }

  private fun setupListeners() {
    viewNewsFiltersShowsChip.setOnCheckedChangeListener { _, _ -> onChipCheckChange() }
    viewNewsFiltersMoviesChip.setOnCheckedChangeListener { _, _ -> onChipCheckChange() }
  }

  private fun clearListeners() {
    viewNewsFiltersShowsChip.setOnCheckedChangeListener(null)
    viewNewsFiltersMoviesChip.setOnCheckedChangeListener(null)
  }

  override fun setEnabled(enabled: Boolean) {
    viewNewsFiltersShowsChip.isEnabled = enabled
    viewNewsFiltersMoviesChip.isEnabled = enabled
  }

  override fun getBehavior() = ScrollableViewBehaviour()
}
