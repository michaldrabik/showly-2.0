package com.michaldrabik.ui_discover.filters.feed

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.screenHeight
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_discover.DiscoverFragment.Companion.REQUEST_DISCOVER_FILTERS
import com.michaldrabik.ui_discover.R
import com.michaldrabik.ui_discover.databinding.ViewDiscoverFiltersFeedBinding
import com.michaldrabik.ui_discover.filters.feed.DiscoverFiltersFeedUiEvent.ApplyFilters
import com.michaldrabik.ui_discover.filters.feed.DiscoverFiltersFeedUiEvent.CloseFilters
import com.michaldrabik.ui_model.DiscoverSortOrder
import com.michaldrabik.ui_model.DiscoverSortOrder.HOT
import com.michaldrabik.ui_model.DiscoverSortOrder.NEWEST
import com.michaldrabik.ui_model.DiscoverSortOrder.RATING
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class DiscoverFiltersFeedBottomSheet : BaseBottomSheetFragment(R.layout.view_discover_filters_feed) {

  private val viewModel by viewModels<DiscoverFiltersFeedViewModel>()
  private val binding by viewBinding(ViewDiscoverFiltersFeedBinding::bind)

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.eventFlow.collect { handleEvent(it) } }
    )
  }

  @SuppressLint("SetTextI18n")
  private fun setupView() {
    val behavior: BottomSheetBehavior<*> = (dialog as BottomSheetDialog).behavior
    behavior.skipCollapsed = true
    behavior.maxHeight = (screenHeight() * 0.9).toInt()

    with(binding) {
      applyButton.onClick { saveFeedOrder() }
    }
  }

  private fun saveFeedOrder() {
    with(binding) {
      val feedOrder = when {
        feedChipHot.isChecked -> HOT
        feedChipTopRated.isChecked -> RATING
        feedChipRecent.isChecked -> NEWEST
        else -> throw IllegalStateException()
      }
      viewModel.saveFeedOrder(feedOrder)
    }
  }

  private fun render(uiState: DiscoverFiltersFeedUiState) {
    with(uiState) {
      feedOrder?.let { renderFilters(it) }
    }
  }

  private fun renderFilters(feedOrder: DiscoverSortOrder) {
    with(binding) {
      feedChipHot.isChecked = feedOrder == HOT
      feedChipTopRated.isChecked = feedOrder == RATING
      feedChipRecent.isChecked = feedOrder == NEWEST
    }
  }

  private fun handleEvent(event: Event<*>) {
    when (event) {
      is ApplyFilters -> {
        setFragmentResult(REQUEST_DISCOVER_FILTERS, Bundle.EMPTY)
        closeSheet()
      }
      is CloseFilters -> closeSheet()
    }
  }
}
