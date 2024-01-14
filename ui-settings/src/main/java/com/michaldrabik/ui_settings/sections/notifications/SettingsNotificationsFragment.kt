package com.michaldrabik.ui_settings.sections.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.NotificationDelay
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_settings.R
import com.michaldrabik.ui_settings.databinding.FragmentSettingsNotificationsBinding
import com.michaldrabik.ui_settings.sections.notifications.SettingsNotificationsUiEvent.RequestNotificationsPermission
import com.michaldrabik.ui_settings.sections.notifications.views.NotificationsRationaleView
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("InlinedApi")
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
      { viewModel.eventFlow.collect { handleEvent(it) } },
      doAfterLaunch = { viewModel.loadSettings(requireAppContext()) }
    )
  }

  private fun setupView() {
    with(binding) {
      settingsShowsNotifications.onClick {
        viewModel.enableNotifications(!settingsShowsNotificationsSwitch.isChecked, requireAppContext())
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

  private fun render(uiState: SettingsNotificationsUiState) {
    uiState.run {
      settings?.let {
        renderSettings(it)
      }
    }
  }

  private fun renderSettings(settings: Settings) {
    with(binding) {
      settingsShowsNotificationsSwitch.isChecked = settings.episodesNotificationsEnabled
      settingsWhenToNotifyValue.run {
        setText(settings.episodesNotificationsDelay.stringRes)
        settingsWhenToNotify.onClick { showWhenToNotifyDialog(settings) }
      }
    }
  }

  private fun handleEvent(event: Event<*>) {
    when (event) {
      is RequestNotificationsPermission -> {
        if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
          showNotificationsRationaleDialog()
        } else {
          requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
      }
    }
  }

  private fun showNotificationsRationaleDialog() {
    val context = requireContext()
    val view = NotificationsRationaleView(context)
    MaterialAlertDialogBuilder(context, R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(context, R.drawable.bg_dialog))
      .setView(view)
      .setPositiveButton(R.string.textYes) { _, _ ->
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
      .setNegativeButton(R.string.textCancel) { _, _ -> }
      .show()
  }

  private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted ->
    if (isGranted) {
      viewModel.enableNotifications(true, requireAppContext())
    }
  }
}
