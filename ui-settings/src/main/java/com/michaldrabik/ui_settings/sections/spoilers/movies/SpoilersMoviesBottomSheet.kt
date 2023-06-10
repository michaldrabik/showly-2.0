package com.michaldrabik.ui_settings.sections.spoilers.movies

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_settings.R
import com.michaldrabik.ui_settings.databinding.SheetSpoilersMoviesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SpoilersMoviesBottomSheet : RemoveTraktBottomSheet<SpoilersMoviesViewModel>(R.layout.sheet_spoilers_movies) {

  private val viewModel by viewModels<SpoilersMoviesViewModel>()
  private val binding by viewBinding(SheetSpoilersMoviesBinding::bind)

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
      myMoviesLayout.onClick {
        viewModel.setHideMyMovies(!myMoviesSwitch.isChecked)
      }
      watchlistMoviesLayout.onClick {
        viewModel.setHideWatchlistMovies(!watchlistMoviesSwitch.isChecked)
      }
      hiddenMoviesLayout.onClick {
        viewModel.setHideHiddenMovies(!hiddenMoviesSwitch.isChecked)
      }
      notCollectedMoviesLayout.onClick {
        viewModel.setHideNotCollectedMovies(!notCollectedMoviesSwitch.isChecked)
      }
      closeButton.onClick { dismiss() }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: SpoilersMoviesUiState) {
    uiState.run {
      with(binding) {
        myMoviesSwitch.isChecked = uiState.isMyMoviesHidden
        watchlistMoviesSwitch.isChecked = uiState.isWatchlistMoviesHidden
        hiddenMoviesSwitch.isChecked = uiState.isHiddenMoviesHidden
        notCollectedMoviesSwitch.isChecked = uiState.isNotCollectedMoviesHidden
      }
    }
  }
}
