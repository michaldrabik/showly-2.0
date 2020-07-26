package com.michaldrabik.showly2.ui.settings

import android.content.Intent
import android.content.Intent.ACTION_SENDTO
import android.content.Intent.ACTION_VIEW
import android.content.Intent.EXTRA_EMAIL
import android.content.Intent.EXTRA_SUBJECT
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.showly2.BuildConfig
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.Config.MY_SHOWS_RECENTS_OPTIONS
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.model.MyShowsSection.FINISHED
import com.michaldrabik.showly2.model.MyShowsSection.RECENTS
import com.michaldrabik.showly2.model.MyShowsSection.UPCOMING
import com.michaldrabik.showly2.model.MyShowsSection.WATCHING
import com.michaldrabik.showly2.model.NotificationDelay
import com.michaldrabik.showly2.model.Settings
import com.michaldrabik.showly2.model.TraktSyncSchedule.OFF
import com.michaldrabik.showly2.requireAppContext
import com.michaldrabik.showly2.ui.common.OnTraktAuthorizeListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.setCheckedSilent
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.fragment_settings.*
import com.michaldrabik.network.Config as ConfigNetwork

class SettingsFragment : BaseFragment<SettingsViewModel>(R.layout.fragment_settings), OnTraktAuthorizeListener {

  override val viewModel by viewModels<SettingsViewModel> { viewModelFactory }

  override fun onCreate(savedInstanceState: Bundle?) {
    fragmentComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, Observer { render(it!!) })
      messageLiveData.observe(viewLifecycleOwner, Observer { showSnack(it) })
      loadSettings()
    }
  }

  private fun setupView() {
    settingsToolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    settingsTraktSync.onClick { navigateTo(R.id.actionSettingsFragmentToTraktSync) }
    settingsDeleteCache.onClick { viewModel.deleteImagesCache(requireAppContext()) }
    settingsRoot.doOnApplyWindowInsets { view, insets, padding, _ ->
      view.updatePadding(top = padding.top + insets.systemWindowInsetTop)
    }
  }

  override fun onResume() {
    super.onResume()
    handleBackPressed()
  }

  private fun render(uiModel: SettingsUiModel) {
    uiModel.run {
      settings?.let { renderSettings(it) }
      isSignedInTrakt?.let { isSignedIn ->
        settingsTraktSync.visibleIf(isSignedIn)
        settingsTraktQuickSync.visibleIf(isSignedIn)
        settingsTraktQuickSyncSwitch.visibleIf(isSignedIn)
        settingsTraktAuthorizeIcon.visibleIf(isSignedIn)
        settingsTraktAuthorize.onClick {
          if (isSignedIn) viewModel.logoutTrakt(requireAppContext())
          else openTraktWebAuthorization()
        }
        val summaryText = when {
          isSignedIn -> {
            when {
              traktUsername?.isNotEmpty() == true ->
                getString(R.string.textSettingsTraktAuthorizeSummarySignOutUser, traktUsername)
              else ->
                getString(R.string.textSettingsTraktAuthorizeSummarySignOut)
            }
          }
          else -> getString(R.string.textSettingsTraktAuthorizeSummarySignIn)
        }
        settingsTraktAuthorizeSummary.text = summaryText
      }
    }
  }

  private fun renderSettings(settings: Settings) {
    settingsRecentShowsAmount.onClick { showRecentShowsDialog(settings) }
    settingsMyShowsSections.onClick { showSectionsDialog(settings) }

    settingsTraktQuickSyncSwitch
      .setCheckedSilent(settings.traktQuickSyncEnabled) { _, isChecked ->
        viewModel.enableQuickSync(isChecked)
        if (isChecked && settings.traktSyncSchedule != OFF) {
          showQuickSyncConfirmationDialog()
        }
      }

    settingsPushNotificationsSwitch
      .setCheckedSilent(settings.pushNotificationsEnabled) { _, isChecked ->
        viewModel.enablePushNotifications(isChecked)
      }

    settingsShowsNotificationsSwitch
      .setCheckedSilent(settings.episodesNotificationsEnabled) { _, isChecked ->
        viewModel.enableEpisodesAnnouncements(isChecked, requireAppContext())
      }

    settingsWhenToNotifyValue.run {
      setText(settings.episodesNotificationsDelay.stringRes)
      onClick { showWhenToNotifyDialog(settings) }
    }

    settingsContactDevs.onClick {
      val intent = Intent(ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(EXTRA_EMAIL, arrayOf(Config.DEVELOPER_MAIL))
        putExtra(EXTRA_SUBJECT, "Showly 2.0 Issue")
      }
      if (intent.resolveActivity(requireActivity().packageManager) != null) {
        startActivity(intent)
      }
    }

    settingsRateApp.onClick {
      val intent = Intent(ACTION_VIEW).apply {
        data = Uri.parse(Config.PLAYSTORE_URL)
        setPackage("com.android.vending")
      }
      if (intent.resolveActivity(requireActivity().packageManager) != null) {
        startActivity(intent)
      }
    }

    settingsVersion.text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
  }

  private fun showQuickSyncConfirmationDialog() {
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setTitle(R.string.textSettingsQuickSyncConfirmationTitle)
      .setMessage(R.string.textSettingsQuickSyncConfirmationMessage)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setPositiveButton(R.string.textTurnOff) { _, _ ->
        viewModel.setTraktSyncSchedule(OFF, requireAppContext())
      }
      .setNegativeButton(R.string.textNotNow) { _, _ -> }
      .show()
  }

  private fun showRecentShowsDialog(settings: Settings) {
    val options = MY_SHOWS_RECENTS_OPTIONS.map { it.toString() }.toTypedArray()
    val default = options.indexOf(settings.myShowsRecentsAmount.toString())

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(options, default) { dialog, index ->
        viewModel.setRecentShowsAmount(options[index].toInt())
        dialog.dismiss()
      }
      .show()
  }

  private fun showSectionsDialog(settings: Settings) {
    val options = listOf(RECENTS, WATCHING, FINISHED, UPCOMING)
    val selected = booleanArrayOf(
      settings.myShowsRecentIsEnabled,
      settings.myShowsRunningIsEnabled,
      settings.myShowsEndedIsEnabled,
      settings.myShowsIncomingIsEnabled
    )
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setMultiChoiceItems(
        options.map { it.displayString }.toTypedArray(),
        selected
      ) { _, index, isChecked ->
        viewModel.enableMyShowsSection(options[index], isChecked)
      }
      .show()
  }

  private fun showWhenToNotifyDialog(settings: Settings) {
    val options = NotificationDelay.values()
    val default = options.indexOf(settings.episodesNotificationsDelay)

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(options.map { getString(it.stringRes) }.toTypedArray(), default) { dialog, index ->
        viewModel.setWhenToNotify(options[index], requireAppContext())
        dialog.dismiss()
      }
      .show()
  }

  private fun openTraktWebAuthorization() {
    Intent(ACTION_VIEW).run {
      data = Uri.parse(ConfigNetwork.TRAKT_AUTHORIZE_URL)
      startActivity(this)
    }
  }

  override fun onAuthorizationResult(authData: Uri?) = viewModel.authorizeTrakt(authData)

  private fun handleBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      remove()
      showNavigation()
      findNavController().popBackStack()
    }
  }

  override fun getSnackbarHost(): ViewGroup = settingsContent
}
