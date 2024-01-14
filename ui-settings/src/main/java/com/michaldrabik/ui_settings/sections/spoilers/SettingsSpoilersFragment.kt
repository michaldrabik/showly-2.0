package com.michaldrabik.ui_settings.sections.spoilers

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.navigateToSafe
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_settings.R
import com.michaldrabik.ui_settings.databinding.FragmentSettingsSpoilersBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsSpoilersFragment :
  BaseFragment<SettingsSpoilersViewModel>(R.layout.fragment_settings_spoilers) {

  override val navigationId = R.id.settingsFragment

  override val viewModel by viewModels<SettingsSpoilersViewModel>()
  private val binding by viewBinding(FragmentSettingsSpoilersBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      doAfterLaunch = { viewModel.loadSettings() }
    )
  }

  private fun setupView() {
    with(binding) {
      settingsSpoilersShows.onClick {
        navigateToSafe(R.id.actionSettingsFragmentToSpoilersShows)
      }
      settingsSpoilersMovies.onClick {
        navigateToSafe(R.id.actionSettingsFragmentToSpoilersMovies)
      }
      settingsSpoilersEpisodes.onClick {
        navigateToSafe(R.id.actionSettingsFragmentToSpoilersEpisodes)
      }
      settingsSpoilersTapToReveal.onClick {
        viewModel.setTapToReveal(!settingsSpoilersTapToRevealSwitch.isChecked)
      }
    }
  }

  private fun render(uiState: SettingsSpoilersUiState) {
    uiState.run {
      with(binding) {
        settingsSpoilersShowsCheck.visibleIf(hasShowsSettingActive)
        settingsSpoilersMoviesCheck.visibleIf(hasMoviesSettingActive)
        settingsSpoilersEpisodesCheck.visibleIf(hasEpisodesSettingActive)
        settingsSpoilersTapToRevealSwitch.isChecked = isTapToReveal
      }
    }
  }

  fun refreshSettings() = viewModel.loadSettings()
}
