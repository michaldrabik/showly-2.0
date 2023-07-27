package com.michaldrabik.ui_settings.sections.spoilers.movies

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.setCheckedSilent
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_settings.R
import com.michaldrabik.ui_settings.SettingsFragment.Companion.REQUEST_SETTINGS
import com.michaldrabik.ui_settings.databinding.SheetSpoilersMoviesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SpoilersMoviesBottomSheet : BaseBottomSheetFragment(R.layout.sheet_spoilers_movies) {

  private val viewModel by viewModels<SpoilersMoviesViewModel>()
  private val binding by viewBinding(SheetSpoilersMoviesBinding::bind)

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

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
      myMoviesDescription.onClick {
        myMoviesListener.invoke(it, !myMoviesSwitch.isChecked)
      }
      myMoviesRatingDescription.onClick {
        myMoviesRatingsListener.invoke(it, !myMoviesRatingsSwitch.isChecked)
      }
      watchlistMoviesDescription.onClick {
        watchlistMoviesListener.invoke(it, !watchlistMoviesSwitch.isChecked)
      }
      watchlistMoviesRatingDescription.onClick {
        watchlistMoviesRatingsListener.invoke(it, !watchlistMoviesRatingsSwitch.isChecked)
      }
      hiddenMoviesDescription.onClick {
        hiddenMoviesListener.invoke(it, !hiddenMoviesSwitch.isChecked)
      }
      hiddenMoviesRatingDescription.onClick {
        hiddenMoviesRatingsListener.invoke(it, !hiddenMoviesRatingsSwitch.isChecked)
      }
      notCollectedMoviesDescription.onClick {
        notCollectedMoviesListener.invoke(it, !notCollectedMoviesSwitch.isChecked)
      }
      notCollectedMoviesRatingDescription.onClick {
        notCollectedMoviesRatingsListener.invoke(it, !notCollectedMoviesRatingsSwitch.isChecked)
      }
      closeButton.onClick { dismiss() }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: SpoilersMoviesUiState) {
    uiState.settings.run {
      with(binding) {
        notCollectedMoviesSwitch.setCheckedSilent(isNotCollectedMoviesHidden, notCollectedMoviesListener)
        notCollectedMoviesRatingsSwitch.setCheckedSilent(isNotCollectedMoviesRatingsHidden, notCollectedMoviesRatingsListener)
        myMoviesSwitch.setCheckedSilent(isMyMoviesHidden, myMoviesListener)
        myMoviesRatingsSwitch.setCheckedSilent(isMyMoviesRatingsHidden, myMoviesRatingsListener)
        watchlistMoviesSwitch.setCheckedSilent(isWatchlistMoviesHidden, watchlistMoviesListener)
        watchlistMoviesRatingsSwitch.setCheckedSilent(isWatchlistMoviesRatingsHidden, watchlistMoviesRatingsListener)
        hiddenMoviesSwitch.setCheckedSilent(isHiddenMoviesHidden, hiddenMoviesListener)
        hiddenMoviesRatingsSwitch.setCheckedSilent(isHiddenMoviesRatingsHidden, hiddenMoviesRatingsListener)
      }
    }
  }

  override fun onDismiss(dialog: DialogInterface) {
    setFragmentResult(REQUEST_SETTINGS, Bundle.EMPTY)
    super.onDismiss(dialog)
  }

  private val notCollectedMoviesListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideNotCollectedMovies(isChecked)
  }
  private val notCollectedMoviesRatingsListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideNotCollectedRatingsMovies(isChecked)
  }
  private val myMoviesListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideMyMovies(isChecked)
  }
  private val myMoviesRatingsListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideMyRatingsMovies(isChecked)
  }
  private val watchlistMoviesListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideWatchlistMovies(isChecked)
  }
  private val watchlistMoviesRatingsListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideWatchlistRatingsMovies(isChecked)
  }
  private val hiddenMoviesListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideHiddenMovies(isChecked)
  }
  private val hiddenMoviesRatingsListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideHiddenRatingsMovies(isChecked)
  }
}
