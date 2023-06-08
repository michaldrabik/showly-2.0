package com.michaldrabik.ui_settings.sections.spoilers.shows

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_settings.R
import com.michaldrabik.ui_settings.databinding.SheetSpoilersShowsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SpoilersShowsBottomSheet : RemoveTraktBottomSheet<SpoilersShowsViewModel>(R.layout.sheet_spoilers_shows) {

  private val viewModel by viewModels<SpoilersShowsViewModel>()
  private val binding by viewBinding(SheetSpoilersShowsBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      doAfterLaunch = { viewModel.refreshSettings() }
    )
  }

  private fun setupView() {
    with(binding) {
      showsOverviewsDetailsLayout.onClick {
        viewModel.setHideDetails(!showsOverviewsDetailsSwitch.isChecked)
      }
      showsOverviewsListsLayout.onClick {
        viewModel.setHideLists(!showsOverviewsListsSwitch.isChecked)
      }
      closeButton.onClick { dismiss() }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: SpoilersShowsUiState) {
    uiState.run {
      with(binding) {
        showsOverviewsDetailsSwitch.isChecked = uiState.isDetailsHidden
        showsOverviewsListsSwitch.isChecked = uiState.isListsHidden
      }
    }
  }
}
