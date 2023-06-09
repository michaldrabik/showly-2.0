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
      myShowsLayout.onClick {
        viewModel.setHideMyShows(!myShowsSwitch.isChecked)
      }
      watchlistShowsLayout.onClick {
        viewModel.setHideWatchlistShows(!watchlistShowsSwitch.isChecked)
      }
      hiddenShowsLayout.onClick {
        viewModel.setHideHiddenShows(!hiddenShowsSwitch.isChecked)
      }
      notCollectedShowsLayout.onClick {
        viewModel.setHideNotCollectedShows(!notCollectedShowsSwitch.isChecked)
      }
      closeButton.onClick { dismiss() }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: SpoilersShowsUiState) {
    uiState.run {
      with(binding) {
        myShowsSwitch.isChecked = uiState.isMyShowsHidden
        watchlistShowsSwitch.isChecked = uiState.isWatchlistShowsHidden
        hiddenShowsSwitch.isChecked = uiState.isHiddenShowsHidden
        notCollectedShowsSwitch.isChecked = uiState.isNotCollectedShowsHidden
      }
    }
  }
}
