package com.michaldrabik.ui_settings

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.michaldrabik.common.Config.SHOW_PREMIUM
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnTraktAuthorizeListener
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_settings.databinding.FragmentSettingsBinding
import com.michaldrabik.ui_settings.sections.spoilers.SettingsSpoilersFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : BaseFragment<SettingsViewModel>(R.layout.fragment_settings), OnTraktAuthorizeListener {

  companion object {
    const val REQUEST_SETTINGS = "REQUEST_SETTINGS"
  }

  override val viewModel by viewModels<SettingsViewModel>()
  private val binding by viewBinding(FragmentSettingsBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setFragmentResultListener(REQUEST_SETTINGS) { _, _ ->
      childFragmentManager.fragments.forEach { fragment ->
        (fragment as? SettingsSpoilersFragment)?.refreshSettings()
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.messageFlow.collect { showSnack(it) } },
      doAfterLaunch = { viewModel.loadSettings() }
    )
  }

  private fun setupView() {
    with(binding) {
      settingsToolbar.setNavigationOnClickListener { activity?.onBackPressed() }
      settingsPremium.onClick { navigateTo(R.id.actionSettingsFragmentToPremium) }
      settingsRoot.doOnApplyWindowInsets { view, insets, padding, _ ->
        val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
        view.updatePadding(top = padding.top + inset)
      }
    }
  }

  private fun render(uiState: SettingsUiState) {
    uiState.run {
      binding.settingsPremium.visibleIf(!isPremium && SHOW_PREMIUM)
    }
  }

  override fun onAuthorizationResult(authData: Uri?) {
    childFragmentManager.fragments.forEach {
      (it as? OnTraktAuthorizeListener)?.onAuthorizationResult(authData)
    }
  }
}
