package com.michaldrabik.ui_settings.sections.spoilers

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.navigateToSafe
import com.michaldrabik.ui_base.utilities.extensions.onClick
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
  }

  private fun setupView() {
    with(binding) {
      settingsSpoilersShows.onClick {
        navigateToSafe(R.id.actionSettingsFragmentToSpoilersShows)
      }
    }
  }
}
