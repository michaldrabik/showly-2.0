package com.michaldrabik.ui_my_shows.myshows.filters

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.forEach
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
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.MyShowsSection.ALL
import com.michaldrabik.ui_model.MyShowsSection.FINISHED
import com.michaldrabik.ui_model.MyShowsSection.UPCOMING
import com.michaldrabik.ui_model.MyShowsSection.WATCHING
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.databinding.ViewMyShowsTypeFiltersBinding
import com.michaldrabik.ui_my_shows.main.FollowedShowsFragment.Companion.REQUEST_MY_SHOWS_FILTERS
import com.michaldrabik.ui_my_shows.myshows.filters.MyShowsFiltersUiEvent.ApplyFilters
import com.michaldrabik.ui_my_shows.myshows.filters.MyShowsFiltersUiEvent.CloseFilters
import com.michaldrabik.ui_my_shows.myshows.filters.views.MyShowsFilterItemView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class MyShowsFiltersBottomSheet : BaseBottomSheetFragment(R.layout.view_my_shows_type_filters) {

  private val viewModel by viewModels<MyShowsFiltersViewModel>()
  private val binding by viewBinding(ViewMyShowsTypeFiltersBinding::bind)

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
      rootItemsLayout.removeAllViews()
      listOf(ALL, WATCHING, UPCOMING, FINISHED)
        .filter { it != MyShowsSection.RECENTS }
        .forEach { section ->
          val itemView = MyShowsFilterItemView(requireContext()).apply {
            onItemClickListener = { toggleItem(it) }
            bind(section, isChecked = false)
          }
          rootItemsLayout.addView(itemView)
        }
      applyButton.onClick { applyFilters() }
    }
  }

  private fun toggleItem(section: MyShowsSection) {
    with(binding) {
      rootItemsLayout.forEach {
        (it as MyShowsFilterItemView).bind(
          sectionType = it.sectionType,
          isChecked = it.sectionType == section
        )
      }
    }
  }

  private fun applyFilters() {
    with(binding) {
      rootItemsLayout.forEach {
        if ((it as MyShowsFilterItemView).isChecked) {
          viewModel.applySectionType(it.sectionType)
        }
      }
    }
  }

  private fun render(uiState: MyShowsFiltersUiState) {
    with(uiState) {
      sectionType?.let { sectionType ->
        binding.rootItemsLayout.forEach {
          (it as MyShowsFilterItemView).bind(
            sectionType = it.sectionType,
            isChecked = it.sectionType == sectionType
          )
        }
      }
    }
  }

  private fun handleEvent(event: Event<*>) {
    when (event) {
      is ApplyFilters -> {
        setFragmentResult(REQUEST_MY_SHOWS_FILTERS, Bundle.EMPTY)
        closeSheet()
      }
      is CloseFilters -> closeSheet()
    }
  }
}
