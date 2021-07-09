package com.michaldrabik.ui_settings

import android.content.Intent
import android.content.Intent.ACTION_SENDTO
import android.content.Intent.EXTRA_EMAIL
import android.content.Intent.EXTRA_SUBJECT
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.processphoenix.ProcessPhoenix
import com.michaldrabik.common.Config
import com.michaldrabik.common.Config.MY_SHOWS_RECENTS_OPTIONS
import com.michaldrabik.common.Config.SHOW_PREMIUM
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_base.common.OnTraktAuthorizeListener
import com.michaldrabik.ui_base.dates.AppDateFormat
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.expandTouch
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.openWebUrl
import com.michaldrabik.ui_base.utilities.extensions.setCheckedSilent
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.MyMoviesSection
import com.michaldrabik.ui_model.MyShowsSection.FINISHED
import com.michaldrabik.ui_model.MyShowsSection.RECENTS
import com.michaldrabik.ui_model.MyShowsSection.UPCOMING
import com.michaldrabik.ui_model.MyShowsSection.WATCHING
import com.michaldrabik.ui_model.NotificationDelay
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_model.TraktSyncSchedule.OFF
import com.michaldrabik.ui_settings.helpers.AppLanguage
import com.michaldrabik.ui_settings.helpers.AppTheme
import com.michaldrabik.ui_settings.helpers.PlayStoreHelper
import com.michaldrabik.ui_settings.helpers.WidgetTransparency
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_settings.*
import com.michaldrabik.data_remote.Config as ConfigNetwork

@AndroidEntryPoint
class SettingsFragment : BaseFragment<SettingsViewModel>(R.layout.fragment_settings), OnTraktAuthorizeListener {

  override val viewModel by viewModels<SettingsViewModel>()

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
    settingsTheme.visibleIf(SHOW_PREMIUM)
    settingsThemeValue.visibleIf(SHOW_PREMIUM)
    settingsNewsEnabled.visibleIf(SHOW_PREMIUM)
    settingsNewsEnabledSwitch.visibleIf(SHOW_PREMIUM)
    settingsWidgetsTheme.visibleIf(SHOW_PREMIUM)
    settingsWidgetsThemeValue.visibleIf(SHOW_PREMIUM)
    settingsWidgetsTransparency.visibleIf(SHOW_PREMIUM)
    settingsWidgetsTransparencyValue.visibleIf(SHOW_PREMIUM)
    settingsTraktQuickRate.visibleIf(SHOW_PREMIUM)
    settingsTraktQuickRateSwitch.visibleIf(SHOW_PREMIUM)
    settingsPremium.onClick { navigateTo(R.id.actionSettingsFragmentToPremium) }
    settingsTraktSync.onClick { navigateTo(R.id.actionSettingsFragmentToTraktSync) }
    settingsDeleteCache.onClick { viewModel.deleteImagesCache(requireAppContext()) }
    settingsTwitterIcon.onClick { openWebLink(Config.TWITTER_URL) }
    settingsTraktIcon.onClick { openWebLink(Config.TRAKT_URL) }
    settingsTmdbIcon.onClick { openWebLink(Config.TMDB_URL) }
    settingsJustWatchIcon.onClick { openWebLink(Config.JUST_WATCH_URL) }
    settingsRoot.doOnApplyWindowInsets { view, insets, padding, _ ->
      view.updatePadding(top = padding.top + insets.systemWindowInsetTop)
    }
  }

  private fun render(uiModel: SettingsUiModel) {
    uiModel.run {
      settings?.let {
        renderSettings(
          it,
          moviesEnabled ?: true,
          newsEnabled ?: false,
          streamingsEnabled ?: true
        )
      }
      language?.let { renderLanguage(it) }
      theme?.let { renderTheme(it) }
      themeWidgets?.let { renderWidgetsTheme(it) }
      widgetsTransparency?.let { renderWidgetsTransparency(it) }
      country?.let { renderCountry(it) }
      dateFormat?.let { renderDateFormat(it) }
      isSigningIn?.let { settingsTraktAuthorizeProgress.visibleIf(it) }
      isSignedInTrakt?.let { isSignedIn ->
        settingsTraktSync.visibleIf(isSignedIn)
        settingsTraktQuickSync.visibleIf(isSignedIn)
        settingsTraktQuickSyncSwitch.visibleIf(isSignedIn)
        settingsTraktQuickRemove.visibleIf(isSignedIn)
        settingsTraktQuickRemoveSwitch.visibleIf(isSignedIn)
        settingsTraktQuickRate.visibleIf(isSignedIn && SHOW_PREMIUM)
        settingsTraktQuickRateSwitch.visibleIf(isSignedIn && SHOW_PREMIUM)
        settingsTraktAuthorizeIcon.visibleIf(isSignedIn)
        settingsTraktAuthorize.onClick {
          if (isSignedIn) showLogoutDialog()
          else openWebUrl(ConfigNetwork.TRAKT_AUTHORIZE_URL)
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
      isPremium?.let { isPremium ->
        settingsPremium.visibleIf(!isPremium && SHOW_PREMIUM)
        listOf(
          settingsTraktQuickRate,
          settingsTraktQuickRateSwitch,
          settingsTheme,
          settingsThemeValue,
          settingsNewsEnabled,
          settingsNewsEnabledSwitch,
          settingsWidgetsTheme,
          settingsWidgetsThemeValue,
          settingsWidgetsTransparency,
          settingsWidgetsTransparencyValue
        ).forEach {
          it.alpha = if (isPremium) 1F else 0.5F
        }

        listOf(
          settingsTraktQuickRateSwitch,
          settingsThemeValue,
          settingsWidgetsThemeValue,
          settingsWidgetsTransparencyValue,
          settingsNewsEnabledSwitch
        ).forEach {
          it.isEnabled = isPremium
        }

        listOf(
          settingsTraktQuickRate,
          settingsTheme,
          settingsWidgetsTheme,
          settingsWidgetsTransparency,
          settingsNewsEnabled
        ).onClick {
          if (!isPremium) navigateTo(R.id.actionSettingsFragmentToPremium)
        }
      }
      userId?.let { settingsUserId.text = it }
      restartApp?.let { if (it) restartApp() }
    }
  }

  private fun renderSettings(
    settings: Settings,
    moviesEnabled: Boolean,
    newsEnabled: Boolean,
    streamingsEnabled: Boolean,
  ) {
    settingsContent.fadeIn(200)
    settingsRecentShowsAmount.onClick { showRecentShowsDialog(settings) }
    settingsMyShowsSections.onClick { showSectionsDialog(settings) }
    settingsMyMoviesSections.onClick { showMoviesSectionsDialog(settings) }

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

    settingsTraktQuickRateSwitch
      .setCheckedSilent(settings.traktQuickRateEnabled) { _, isChecked ->
        viewModel.enableQuickRate(isChecked)
      }

    settingsPushNotificationsSwitch
      .setCheckedSilent(settings.pushNotificationsEnabled) { _, isChecked ->
        viewModel.enablePushNotifications(isChecked)
      }

    settingsShowsNotificationsSwitch
      .setCheckedSilent(settings.episodesNotificationsEnabled) { _, isChecked ->
        viewModel.enableAnnouncements(isChecked)
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

    settingsUpcomingSectionSwitch
      .setCheckedSilent(settings.progressUpcomingEnabled) { _, isChecked ->
        viewModel.enableProgressUpcoming(isChecked, requireAppContext())
      }

    settingsMoviesEnabledSwitch
      .setCheckedSilent(moviesEnabled) { _, isChecked ->
        viewModel.enableMovies(isChecked)
      }

    settingsNewsEnabledSwitch
      .setCheckedSilent(newsEnabled) { _, isChecked ->
        viewModel.enableNews(isChecked)
      }

    settingsStreamingsSwitch
      .setCheckedSilent(streamingsEnabled) { _, isChecked ->
        viewModel.enableStreamings(isChecked)
      }

    settingsWidgetsLabelsSwitch
      .setCheckedSilent(settings.widgetsShowLabel) { _, isChecked ->
        viewModel.enableWidgetsTitles(isChecked, requireAppContext())
      }

    settingsContactDevs.onClick {
      val intent = Intent(ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(EXTRA_EMAIL, arrayOf(Config.DEVELOPER_MAIL))
        putExtra(EXTRA_SUBJECT, "Showly 2.0 Message")
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

  private fun renderTheme(theme: AppTheme) {
    settingsThemeValue.run {
      expandTouch()
      setText(theme.displayName)
      onClick { showThemeDialog(theme) }
    }
  }

  private fun renderWidgetsTheme(theme: AppTheme) {
    settingsWidgetsThemeValue.run {
      expandTouch()
      setText(theme.displayName)
      onClick { showWidgetsThemeDialog(theme) }
    }
  }

  private fun renderWidgetsTransparency(value: WidgetTransparency) {
    settingsWidgetsTransparencyValue.run {
      expandTouch()
      setText(value.displayName)
      onClick { showWidgetsTransparencyDialog(value) }
    }
  }

  private fun renderCountry(country: AppCountry) {
    settingsCountryValue.run {
      text = country.displayName
      onClick { showCountryDialog(country) }
    }
  }

  private fun renderDateFormat(format: AppDateFormat) {
    settingsDateFormat.run {
      settingsDateFormatValue.text = DateFormatProvider.loadSettingsFormat(format).format(nowUtc().toLocalZone())
      onClick { showDateFormatDialog(format) }
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

  private fun showMoviesSectionsDialog(settings: Settings) {
    val options = listOf(MyMoviesSection.RECENTS)
    val selected = booleanArrayOf(
      settings.myMoviesRecentIsEnabled
    )
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setMultiChoiceItems(
        options.map { getString(it.displayString) }.toTypedArray(),
        selected
      ) { _, index, isChecked ->
        viewModel.enableMyMoviesSection(options[index], isChecked)
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

  private fun showCountryDialog(country: AppCountry) {
    val options = AppCountry.values()
    val selected = options.indexOf(country)

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(options.map { it.displayName }.toTypedArray(), selected) { dialog, index ->
        if (index != selected) {
          viewModel.setCountry(options[index])
        }
        dialog.dismiss()
      }
      .show()
  }

  private fun showDateFormatDialog(format: AppDateFormat) {
    val options = AppDateFormat.values()
    val selected = options.indexOf(format)

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog_SmallText)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(
        options.map {
          DateFormatProvider.loadSettingsFormat(it).format(nowUtc().toLocalZone())
        }.toTypedArray(),
        selected
      ) { dialog, index ->
        if (index != selected) {
          viewModel.setDateFormat(options[index], requireAppContext())
        }
        dialog.dismiss()
      }
      .show()
  }

  private fun showThemeDialog(theme: AppTheme) {
    val options = AppTheme.values()
    val selected = options.indexOf(theme)
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(options.map { getString(it.displayName) }.toTypedArray(), selected) { dialog, index ->
        if (index != selected) {
          viewModel.setTheme(options[index])
          setDefaultNightMode(options[index].code)
        }
        dialog.dismiss()
      }
      .show()
  }

  private fun showWidgetsThemeDialog(theme: AppTheme) {
    val options = AppTheme.values().filter { it != AppTheme.SYSTEM }
    val selected = options.indexOf(theme)
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(options.map { getString(it.displayName) }.toTypedArray(), selected) { dialog, index ->
        if (index != selected) {
          viewModel.setWidgetsTheme(options[index], requireAppContext())
        }
        dialog.dismiss()
      }
      .show()
  }

  private fun showWidgetsTransparencyDialog(transparency: WidgetTransparency) {
    val options = WidgetTransparency.values()
    val selected = options.indexOf(transparency)
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(options.map { getString(it.displayName) }.toTypedArray(), selected) { dialog, index ->
        if (index != selected) {
          viewModel.setWidgetsTransparency(options[index], requireAppContext())
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
        viewModel.setWhenToNotify(options[index])
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

  private fun restartApp() {
    try {
      ProcessPhoenix.triggerRebirth(requireAppContext())
    } catch (error: Throwable) {
      Runtime.getRuntime().exit(0)
    }
  }

  private fun openWebLink(url: String) {
    openWebUrl(url) ?: showSnack(MessageEvent.info(R.string.errorCouldNotFindApp))
  }

  override fun onAuthorizationResult(authData: Uri?) = viewModel.authorizeTrakt(authData)
}
