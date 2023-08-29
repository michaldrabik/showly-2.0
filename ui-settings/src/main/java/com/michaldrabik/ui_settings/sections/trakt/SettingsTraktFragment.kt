package com.michaldrabik.ui_settings.sections.trakt

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.work.WorkInfo.State
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.common.Config
import com.michaldrabik.data_remote.Config.TRAKT_AUTHORIZE_URL
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnTraktAuthorizeListener
import com.michaldrabik.ui_base.trakt.TraktSyncWorker
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.openWebUrl
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.PremiumFeature
import com.michaldrabik.ui_model.TraktSyncSchedule.OFF
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ITEM
import com.michaldrabik.ui_settings.R
import com.michaldrabik.ui_settings.databinding.FragmentSettingsTraktBinding
import com.michaldrabik.ui_settings.sections.trakt.SettingsTraktUiEvent.RequestNotificationsPermission
import com.michaldrabik.ui_settings.sections.trakt.SettingsTraktUiEvent.StartAuthorization
import com.michaldrabik.ui_settings.sections.trakt.views.TraktNotificationsRationaleView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsTraktFragment :
  BaseFragment<SettingsTraktViewModel>(R.layout.fragment_settings_trakt),
  OnTraktAuthorizeListener {

  override val viewModel by viewModels<SettingsTraktViewModel>()
  private val binding by viewBinding(FragmentSettingsTraktBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupWorkManager()
    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.eventFlow.collect { handleEvent(it) } },
      { viewModel.messageFlow.collect { showSnack(it) } },
      doAfterLaunch = { viewModel.loadSettings() }
    )
  }

  private fun setupView() {
    with(binding) {
      settingsTraktQuickRate.visibleIf(Config.SHOW_PREMIUM)
      settingsTraktQuickRemove.onClick {
        viewModel.enableQuickRemove(!settingsTraktQuickRemoveSwitch.isChecked)
      }
      settingsTraktSync.onClick {
        navigateTo(R.id.actionSettingsFragmentToTraktSync)
      }
    }
  }

  private fun setupWorkManager() {
    WorkManager.getInstance(requireAppContext())
      .getWorkInfosByTagLiveData(TraktSyncWorker.TAG_ID)
      .observe(viewLifecycleOwner) {
        binding.settingsTraktSyncProgress.visibleIf(it.any { work -> work.state == State.RUNNING })
      }
  }

  private fun render(uiState: SettingsTraktUiState) {
    uiState.run {
      with(binding) {
        settingsTraktAuthorizeProgress.visibleIf(isSigningIn)
        settingsTraktSync.visibleIf(isSignedInTrakt)
        settingsTraktQuickSync.visibleIf(isSignedInTrakt)
        settingsTraktQuickRemove.visibleIf(isSignedInTrakt)
        settingsTraktQuickRate.visibleIf(isSignedInTrakt && Config.SHOW_PREMIUM)
        settingsTraktIcon.visibleIf(!isSignedInTrakt && !isSigningIn)
        settingsTraktAuthorizeIcon.visibleIf(isSignedInTrakt)

        settingsTraktQuickRateSwitch.isChecked = settings?.traktQuickRateEnabled ?: false
        settingsTraktQuickRate.alpha = if (isPremium) 1F else 0.5F
        settingsTraktQuickRate.onClick { view ->
          onPremiumAction(isPremium, view.tag) {
            viewModel.enableQuickRate(!settingsTraktQuickRateSwitch.isChecked)
          }
        }
        if (isPremium) {
          settingsTraktQuickRateTitle.setCompoundDrawables(null, null, null, null)
        }

        settingsTraktQuickRemoveSwitch.isChecked = settings?.traktQuickRemoveEnabled ?: false
        settingsTraktQuickRemove.onClick {
          viewModel.enableQuickRemove(!settingsTraktQuickRemoveSwitch.isChecked)
        }

        settingsTraktQuickSyncSwitch.isChecked = settings?.traktQuickSyncEnabled ?: false
        settingsTraktQuickSync.onClick {
          val isChecked = settingsTraktQuickSyncSwitch.isChecked
          viewModel.enableQuickSync(!isChecked)
          if (!isChecked && settings?.traktSyncSchedule != OFF) {
            showQuickSyncConfirmationDialog()
          }
        }

        settingsTraktAuthorize.onClick {
          if (isSignedInTrakt) {
            showLogoutDialog()
          } else if (!isSigningIn) {
            viewModel.startAuthorization(requireAppContext())
          }
        }

        settingsTraktAuthorizeSummary.text = when {
          isSignedInTrakt -> when {
            traktUsername.isNotEmpty() -> getString(R.string.textSettingsTraktAuthorizeSummarySignOutUser, traktUsername)
            else -> getString(R.string.textSettingsTraktAuthorizeSummarySignOut)
          }
          else -> getString(R.string.textSettingsTraktAuthorizeSummarySignIn)
        }
      }
    }
  }

  private fun handleEvent(event: Event<*>) {
    when (event) {
      StartAuthorization -> openTraktAuthWebsite()
      RequestNotificationsPermission -> showNotificationsRationaleDialog()
    }
  }

  private fun onPremiumAction(
    isPremium: Boolean,
    tag: Any?,
    action: () -> Unit,
  ) {
    if (isPremium) {
      action()
      return
    }
    val args = bundleOf()
    if (tag != null) {
      val feature = PremiumFeature.fromTag(requireContext(), tag.toString())
      feature?.let {
        args.putSerializable(ARG_ITEM, feature)
      }
    }
    navigateTo(R.id.actionSettingsFragmentToPremium, args)
  }

  private fun showQuickSyncConfirmationDialog() {
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setTitle(R.string.textSettingsQuickSyncConfirmationTitle)
      .setMessage(R.string.textSettingsQuickSyncConfirmationMessage)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setPositiveButton(R.string.textTurnOff) { _, _ ->
        viewModel.setTraktSyncSchedule(OFF)
      }
      .setNegativeButton(R.string.textNotNow) { _, _ -> }
      .show()
  }

  private fun showLogoutDialog() {
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setTitle(R.string.textSettingsLogoutTitle)
      .setMessage(R.string.textSettingsLogoutMessage)
      .setPositiveButton(R.string.textYes) { _, _ ->
        viewModel.logoutTrakt()
      }
      .setNegativeButton(R.string.textCancel) { _, _ -> }
      .show()
  }

  @SuppressLint("InlinedApi")
  private fun showNotificationsRationaleDialog() {
    val context = requireContext()
    val view = TraktNotificationsRationaleView(context)
    MaterialAlertDialogBuilder(context, R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(context, R.drawable.bg_dialog))
      .setView(view)
      .setPositiveButton(R.string.textYes) { _, _ ->
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
      .setNegativeButton(R.string.textNo) { _, _ -> openTraktAuthWebsite() }
      .show()
  }

  private fun openTraktAuthWebsite() {
    openWebUrl(TRAKT_AUTHORIZE_URL) ?: showSnack(MessageEvent.Error(R.string.errorCouldNotFindApp))
  }

  override fun onAuthorizationResult(authData: Uri?) {
    viewModel.authorizeTrakt(authData)
  }

  private val requestPermissionLauncher =
    registerForActivityResult(RequestPermission()) { _ ->
      openTraktAuthWebsite()
    }
}
