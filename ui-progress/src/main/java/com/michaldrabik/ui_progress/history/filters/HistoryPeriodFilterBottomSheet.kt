package com.michaldrabik.ui_progress.history.filters

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.fragment.app.setFragmentResult
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.requireSerializable
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.HistoryPeriod
import com.michaldrabik.ui_model.HistoryPeriod.ALL_TIME
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.databinding.FragmentHistoryPeriodFilterBinding
import com.michaldrabik.ui_progress.history.filters.views.HistoryPeriodItemView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryPeriodFilterBottomSheet : BaseBottomSheetFragment(R.layout.fragment_history_period_filter) {

  companion object {
    const val REQUEST_KEY = "REQUEST_KEY_HISTORY_DATES_FILTER"
    const val ARG_SELECTED_FILTER = "ARG_SELECTED_ITEM"

    fun createBundle(selectedItem: HistoryPeriod): Bundle {
      return bundleOf(ARG_SELECTED_FILTER to selectedItem)
    }
  }

  private val binding by viewBinding(FragmentHistoryPeriodFilterBinding::bind)

  private lateinit var initialPeriod: HistoryPeriod
  private lateinit var selectedPeriod: HistoryPeriod

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initialPeriod = requireSerializable(ARG_SELECTED_FILTER)
    selectedPeriod = initialPeriod
    setupView()
  }

  private fun setupView() {
    with(binding) {
      itemsLayout.removeAllViews()
      HistoryPeriod.entries
        .filterNot { it == ALL_TIME }
        .forEach { item ->
          val view = HistoryPeriodItemView(requireContext()).apply {
            bind(item, isChecked = item == initialPeriod)
            onItemClick = {
              selectedPeriod = it
              itemsLayout.children.forEach { view ->
                (view as HistoryPeriodItemView).bind(view.item, view.item == selectedPeriod)
              }
            }
          }
          itemsLayout.addView(view)
        }
      applyButton.onClick { applyFilter() }
    }
  }

  private fun applyFilter() {
    if (selectedPeriod == initialPeriod) {
      closeSheet()
      return
    }
    val result = bundleOf(ARG_SELECTED_FILTER to selectedPeriod)
    setFragmentResult(REQUEST_KEY, result)
    closeSheet()
  }
}
