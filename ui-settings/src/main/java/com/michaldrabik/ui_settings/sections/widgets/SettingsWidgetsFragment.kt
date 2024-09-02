package com.michaldrabik.ui_settings.sections.widgets

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.PremiumFeature
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ITEM
import com.michaldrabik.ui_settings.R
import com.michaldrabik.ui_settings.databinding.FragmentSettingsWidgetsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsWidgetsFragment :
  BaseFragment<SettingsWidgetsViewModel>(R.layout.fragment_settings_widgets) {

  override val viewModel by viewModels<SettingsWidgetsViewModel>()
  private val binding by viewBinding(FragmentSettingsWidgetsBinding::bind)

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      doAfterLaunch = { viewModel.loadSettings() },
    )
  }

  private fun setupView() {
    with(binding) {
      settingsWidgetsLabels.onClick {
        viewModel.enableWidgetsTitles(!settingsWidgetsLabelsSwitch.isChecked, requireAppContext())
      }
    }
  }

  private fun render(uiState: SettingsWidgetsUiState) {
    uiState.run {
      with(binding) {
        settings?.let {
          settingsWidgetsLabelsSwitch.isChecked = it.widgetsShowLabel
        }
        themeWidgets?.let {
          settingsWidgetsThemeValue.setText(it.displayName)
          settingsWidgetsTheme.onClick { view ->
            openPremiumScreen(view.tag)
          }
        }
        widgetsTransparency.let {
          settingsWidgetsTransparencyValue.setText(it.displayName)
          settingsWidgetsTransparency.onClick { view ->
            openPremiumScreen(view.tag)
          }
        }
        isPremium.let {
          val alpha = if (it) 1F else 0.5F
          settingsWidgetsTheme.alpha = alpha
          settingsWidgetsThemeValue.alpha = alpha
          settingsWidgetsTransparency.alpha = alpha
          if (it) {
            settingsWidgetsThemeTitle.setCompoundDrawables(null, null, null, null)
            settingsWidgetsTransparencyTitle.setCompoundDrawables(null, null, null, null)
          }
        }
      }
    }
  }

  private fun openPremiumScreen(tag: Any?) {
    val args = bundleOf()
    if (tag != null) {
      val feature = PremiumFeature.fromTag(requireContext(), tag.toString())
      feature?.let {
        args.putSerializable(ARG_ITEM, feature)
      }
    }
    navigateTo(R.id.actionSettingsFragmentToPremium, args)
  }
}
