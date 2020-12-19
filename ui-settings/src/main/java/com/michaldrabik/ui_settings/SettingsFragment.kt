package com.michaldrabik.ui_settings

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.processphoenix.ProcessPhoenix
import com.michaldrabik.common.Config
import com.michaldrabik.common.Config.MY_SHOWS_RECENTS_OPTIONS
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnTraktAuthorizeListener
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.setCheckedSilent
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.MyShowsSection.FINISHED
import com.michaldrabik.ui_model.MyShowsSection.RECENTS
import com.michaldrabik.ui_model.MyShowsSection.UPCOMING
import com.michaldrabik.ui_model.MyShowsSection.WATCHING
import com.michaldrabik.ui_model.NotificationDelay
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_model.TraktSyncSchedule.OFF
import com.michaldrabik.ui_settings.di.UiSettingsComponentProvider
import com.michaldrabik.ui_settings.helpers.AppLanguage
import com.michaldrabik.ui_settings.helpers.PlayStoreHelper
import kotlinx.android.synthetic.main.fragment_settings.*
import com.michaldrabik.network.Config as ConfigNetwork

class SettingsFragment : BaseFragment<SettingsViewModel>(R.layout.fragment_settings), OnTraktAuthorizeListener {

  override val viewModel by viewModels<SettingsViewModel> { viewModelFactory }

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiSettingsComponentProvider).provideSettingsComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner) { render(it!!) }
      messageLiveData.observe(viewLifecycleOwner) { showSnack(it) }
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
      settings?.let { renderSettings(it, moviesEnabled ?: true) }
      language?.let { renderLanguage(it) }
      isSignedInTrakt?.let { isSignedIn ->
        settingsTraktSync.visibleIf(isSignedIn)
        settingsTraktQuickSync.visibleIf(isSignedIn)
        settingsTraktQuickSyncSwitch.visibleIf(isSignedIn)
        settingsTraktQuickRemove.visibleIf(isSignedIn)
        settingsTraktQuickRemoveSwitch.visibleIf(isSignedIn)
        settingsTraktAuthorizeIcon.visibleIf(isSignedIn)
        settingsTraktAuthorize.onClick {
          if (isSignedIn) showLogoutDialog()
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
      restartApp?.let { if (it) restartApp() }
    }
  }

  private fun renderSettings(settings: Settings, moviesEnabled: Boolean) {
    settingsContent.fadeIn(200)
    settingsRecentShowsAmount.onClick { showRecentShowsDialog(settings) }
    settingsMyShowsSections.onClick { showSectionsDialog(settings) }

    settingsTraktQuickSyncSwitch
      .setCheckedSilent(settings.traktQuickSyncEnabled) { _, isChecked ->
        viewModel.enableQuickSync(isChecked)
        if (isChecked && settings.traktSyncSchedule != OFF) {
          showQuickSyncConfirmationDialog()
        }
      }

    settingsTraktQuickRemoveSwitch
      .setCheckedSilent(settings.traktQuickRemoveEnabled) { _, isChecked ->
        viewModel.enableQuickRemove(isChecked)
      }

    settingsPushNotificationsSwitch
      .setCheckedSilent(settings.pushNotificationsEnabled) { _, isChecked ->
        viewModel.enablePushNotifications(isChecked)
      }

    settingsShowsNotificationsSwitch
      .setCheckedSilent(settings.episodesNotificationsEnabled) { _, isChecked ->
        viewModel.enableAnnouncements(isChecked, requireAppContext())
      }

    settingsWhenToNotifyValue.run {
      setText(settings.episodesNotificationsDelay.stringRes)
      onClick { showWhenToNotifyDialog(settings) }
    }

    settingsIncludeArchivedStatsSwitch
      .setCheckedSilent(settings.archiveShowsIncludeStatistics) { _, isChecked ->
        viewModel.enableArchivedStatistics(isChecked)
      }

    settingsIncludeSpecialsSwitch
      .setCheckedSilent(settings.specialSeasonsEnabled) { _, isChecked ->
        viewModel.enableSpecialSeasons(isChecked)
      }

    settingsMoviesEnabledSwitch
      .setCheckedSilent(moviesEnabled) { _, isChecked ->
        viewModel.enableMovies(isChecked, requireAppContext())
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
      PlayStoreHelper.openPlayStorePage(requireActivity())
    }

    settingsVersion.text = "v${BuildConfig.VER_NAME} (${BuildConfig.VER_CODE})"
  }

  private fun renderLanguage(language: AppLanguage) {
    settingsLanguageValue.run {
      setText(language.displayName)
      onClick { showLanguageDialog(language) }
    }
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
    val default = options.indexOf(settings.myRecentsAmount.toString())

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
        options.map { getString(it.displayString) }.toTypedArray(),
        selected
      ) { _, index, isChecked ->
        viewModel.enableMyShowsSection(options[index], isChecked)
      }
      .show()
  }

  private fun showLanguageDialog(language: AppLanguage) {
    val options = AppLanguage.values()
    val selected = options.indexOf(language)

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(options.map { getString(it.displayName) }.toTypedArray(), selected) { dialog, index ->
        if (index != selected) {
          viewModel.setLanguage(options[index])
        }
        dialog.dismiss()
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

  private fun showLogoutDialog() {
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setTitle(R.string.textSettingsLogoutTitle)
      .setMessage(R.string.textSettingsLogoutMessage)
      .setPositiveButton(R.string.textYes) { _, _ ->
        viewModel.logoutTrakt(requireAppContext())
      }
      .setNegativeButton(R.string.textCancel) { _, _ -> }
      .show()
  }

  private fun openTraktWebAuthorization() {
    Intent(ACTION_VIEW).run {
      data = Uri.parse(ConfigNetwork.TRAKT_AUTHORIZE_URL)
      startActivity(this)
    }
  }

  private fun restartApp() {
    try {
      ProcessPhoenix.triggerRebirth(requireAppContext())
    } catch (error: Throwable) {
      Runtime.getRuntime().exit(0)
    }
  }

  override fun onAuthorizationResult(authData: Uri?) = viewModel.authorizeTrakt(authData)

  private fun handleBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      remove()
      showNavigation()
      findNavControl().popBackStack()
    }
  }

  override fun getSnackbarHost(): ViewGroup = settingsContent
}
