package com.michaldrabik.ui_settings.sections.notifications

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.NotificationDelay
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_settings.R
import com.michaldrabik.ui_settings.databinding.FragmentSettingsNotificationsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsNotificationsFragment :
  BaseFragment<SettingsNotificationsViewModel>(R.layout.fragment_settings_notifications) {

  override val viewModel by viewModels<SettingsNotificationsViewModel>()
  private val binding by viewBinding(FragmentSettingsNotificationsBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      doAfterLaunch = { viewModel.loadSettings() }
    )
  }

  private fun render(uiState: SettingsNotificationsUiState) {
    uiState.run {
      settings?.let {
        renderSettings(it)
      }
    }
  }

  private fun setupView() {
    with(binding) {
      settingsShowsNotifications.onClick {
        viewModel.enableAnnouncements(!settingsShowsNotificationsSwitch.isChecked)
      }
      settingsPushNotifications.onClick {
        viewModel.enablePushNotifications(!settingsPushNotificationsSwitch.isChecked)
      }
      settingsShowsNotifications.onClick {
        viewModel.enableAnnouncements(!settingsShowsNotificationsSwitch.isChecked)
      }
    }
  }

  private fun renderSettings(settings: Settings) {
    with(binding) {
      settingsPushNotificationsSwitch.isChecked = settings.pushNotificationsEnabled
      settingsShowsNotificationsSwitch.isChecked = settings.episodesNotificationsEnabled
      settingsWhenToNotifyValue.run {
        setText(settings.episodesNotificationsDelay.stringRes)
        settingsWhenToNotify.onClick { showWhenToNotifyDialog(settings) }
      }
    }
  }

  private fun showWhenToNotifyDialog(settings: Settings) {
    val options = NotificationDelay.values()
    val default = options.indexOf(settings.episodesNotificationsDelay)

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(options.map { getString(it.stringRes) }.toTypedArray(), default) { dialog, index ->
        viewModel.setWhenToNotify(options[index])
        dialog.dismiss()
      }
      .show()
  }
}
