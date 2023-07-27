package com.michaldrabik.ui_settings.sections.spoilers.episodes

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
import com.michaldrabik.ui_settings.databinding.SheetSpoilersEpisodesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SpoilersEpisodesBottomSheet : BaseBottomSheetFragment(R.layout.sheet_spoilers_episodes) {

  private val viewModel by viewModels<SpoilersEpisodesViewModel>()
  private val binding by viewBinding(SheetSpoilersEpisodesBinding::bind)

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
      episodesHideTitle.onClick {
        hideTitleListener.invoke(it, !episodesHideTitleSwitch.isChecked)
      }
      episodesHideDescription.onClick {
        hideDescriptionListener.invoke(it, !episodesHideDescriptionSwitch.isChecked)
      }
      episodesHideRating.onClick {
        hideRatingListener.invoke(it, !episodesHideRatingSwitch.isChecked)
      }
      episodesHideImages.onClick {
        hideImageListener.invoke(it, !episodesHideImagesSwitch.isChecked)
      }
      closeButton.onClick { dismiss() }
    }
  }

  private fun render(uiState: SpoilersEpisodesUiState) {
    uiState.settings.run {
      with(binding) {
        episodesHideTitleSwitch.setCheckedSilent(isEpisodeTitleHidden, hideTitleListener)
        episodesHideDescriptionSwitch.setCheckedSilent(isEpisodeDescriptionHidden, hideDescriptionListener)
        episodesHideRatingSwitch.setCheckedSilent(isEpisodeRatingHidden, hideRatingListener)
        episodesHideImagesSwitch.setCheckedSilent(isEpisodeImageHidden, hideImageListener)
      }
    }
  }

  override fun onDismiss(dialog: DialogInterface) {
    setFragmentResult(REQUEST_SETTINGS, Bundle.EMPTY)
    super.onDismiss(dialog)
  }

  private val hideTitleListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideTitle(isChecked)
  }
  private val hideDescriptionListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideDescription(isChecked)
  }
  private val hideRatingListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideRating(isChecked)
  }
  private val hideImageListener: (View, Boolean) -> Unit = { _, isChecked ->
    viewModel.setHideImage(isChecked)
  }
}
