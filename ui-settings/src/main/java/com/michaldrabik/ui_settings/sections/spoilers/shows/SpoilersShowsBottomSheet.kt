package com.michaldrabik.ui_settings.sections.spoilers.shows

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.setCheckedSilent
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
      myShowsDescription.onClick {
        myShowsListener.invoke(it, !myShowsSwitch.isChecked)
      }
      myShowsRatingDescription.onClick {
        myShowsRatingsListener.invoke(it, !myShowsRatingsSwitch.isChecked)
      }
      watchlistShowsDescription.onClick {
        watchlistShowsListener.invoke(it, !watchlistShowsSwitch.isChecked)
      }
      watchlistShowsRatingDescription.onClick {
        watchlistShowsRatingsListener.invoke(it, !watchlistShowsRatingsSwitch.isChecked)
      }
      hiddenShowsDescription.onClick {
        hiddenShowsListener.invoke(it, !hiddenShowsSwitch.isChecked)
      }
      hiddenShowsRatingDescription.onClick {
        hiddenShowsRatingsListener.invoke(it, !hiddenShowsRatingsSwitch.isChecked)
      }
      notCollectedShowsDescription.onClick {
        notCollectedShowsListener.invoke(it, !notCollectedShowsSwitch.isChecked)
      }
      notCollectedShowsRatingDescription.onClick {
        notCollectedShowsRatingsListener.invoke(it, !notCollectedShowsRatingsSwitch.isChecked)
      }
      closeButton.onClick { dismiss() }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: SpoilersShowsUiState) {
    uiState.settings.run {
      with(binding) {
        notCollectedShowsSwitch.setCheckedSilent(isNotCollectedShowsHidden, notCollectedShowsListener)
        notCollectedShowsRatingsSwitch.setCheckedSilent(isNotCollectedShowsRatingsHidden, notCollectedShowsRatingsListener)
        myShowsSwitch.setCheckedSilent(isMyShowsHidden, myShowsListener)
        myShowsRatingsSwitch.setCheckedSilent(isMyShowsRatingsHidden, myShowsRatingsListener)
        watchlistShowsSwitch.setCheckedSilent(isWatchlistShowsHidden, watchlistShowsListener)
        watchlistShowsRatingsSwitch.setCheckedSilent(isWatchlistShowsRatingsHidden, watchlistShowsRatingsListener)
        hiddenShowsSwitch.setCheckedSilent(isHiddenShowsHidden, hiddenShowsListener)
        hiddenShowsRatingsSwitch.setCheckedSilent(isHiddenShowsRatingsHidden, hiddenShowsRatingsListener)
      }
    }
  }

  private val notCollectedShowsListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideNotCollectedShows(isChecked)
  }
  private val notCollectedShowsRatingsListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideNotCollectedRatingsShows(isChecked)
  }
  private val myShowsListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideMyShows(isChecked)
  }
  private val myShowsRatingsListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideMyRatingsShows(isChecked)
  }
  private val watchlistShowsListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideWatchlistShows(isChecked)
  }
  private val watchlistShowsRatingsListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideWatchlistRatingsShows(isChecked)
  }
  private val hiddenShowsListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideHiddenShows(isChecked)
  }
  private val hiddenShowsRatingsListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideHiddenRatingsShows(isChecked)
  }
}
