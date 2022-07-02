package com.michaldrabik.ui_news.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.michaldrabik.ui_base.common.behaviour.ScrollableViewBehaviour
import com.michaldrabik.ui_model.NewsItem
import com.michaldrabik.ui_news.databinding.ViewNewsFiltersBinding

class NewsFiltersView : FrameLayout, CoordinatorLayout.AttachedBehavior {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewNewsFiltersBinding.inflate(LayoutInflater.from(context), this)

  init {
    setupListeners()
  }

  var onChipsChangeListener: ((List<NewsItem.Type>) -> Unit)? = null

  private fun onChipCheckChange() {
    val ids = binding.viewNewsFiltersChipGroup.checkedChipIds.map {
      when (it) {
        binding.viewNewsFiltersShowsChip.id -> NewsItem.Type.SHOW
        binding.viewNewsFiltersMoviesChip.id -> NewsItem.Type.MOVIE
        else -> throw IllegalStateException()
      }
    }
    onChipsChangeListener?.invoke(ids)
  }

  fun setFilters(filters: List<NewsItem.Type>) {
    clearListeners()

    binding.viewNewsFiltersShowsChip.isChecked = filters.contains(NewsItem.Type.SHOW)
    binding.viewNewsFiltersMoviesChip.isChecked = filters.contains(NewsItem.Type.MOVIE)

    setupListeners()
  }

  private fun setupListeners() {
    binding.viewNewsFiltersShowsChip.setOnCheckedChangeListener { _, _ -> onChipCheckChange() }
    binding.viewNewsFiltersMoviesChip.setOnCheckedChangeListener { _, _ -> onChipCheckChange() }
  }

  private fun clearListeners() {
    binding.viewNewsFiltersShowsChip.setOnCheckedChangeListener(null)
    binding.viewNewsFiltersMoviesChip.setOnCheckedChangeListener(null)
  }

  override fun setEnabled(enabled: Boolean) {
    binding.viewNewsFiltersShowsChip.isEnabled = enabled
    binding.viewNewsFiltersMoviesChip.isEnabled = enabled
  }

  override fun getBehavior() = ScrollableViewBehaviour()
}
