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
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
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
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.expandTouch
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.openWebUrl
import com.michaldrabik.ui_base.utilities.extensions.setCheckedSilent
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.MyMoviesSection
import com.michaldrabik.ui_model.MyShowsSection.RECENTS
import com.michaldrabik.ui_model.PremiumFeature
import com.michaldrabik.ui_model.ProgressNextEpisodeType
import com.michaldrabik.ui_model.ProgressNextEpisodeType.LAST_WATCHED
import com.michaldrabik.ui_model.ProgressNextEpisodeType.OLDEST
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ITEM
import com.michaldrabik.ui_settings.helpers.AppLanguage
import com.michaldrabik.ui_settings.helpers.AppTheme
import com.michaldrabik.ui_settings.helpers.PlayStoreHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_settings.*

@AndroidEntryPoint
class SettingsFragment : BaseFragment<SettingsViewModel>(R.layout.fragment_settings), OnTraktAuthorizeListener {

  override val viewModel by viewModels<SettingsViewModel>()

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
    settingsToolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    settingsTheme.visibleIf(SHOW_PREMIUM)
    settingsThemeValue.visibleIf(SHOW_PREMIUM)
    settingsNewsEnabled.visibleIf(SHOW_PREMIUM)
    settingsNewsEnabledSwitch.visibleIf(SHOW_PREMIUM)
    settingsPremium.onClick { navigateTo(R.id.actionSettingsFragmentToPremium) }
    settingsDeleteCache.onClick { viewModel.deleteImagesCache(requireAppContext()) }
    settingsTwitterIcon.onClick { openWebLink(Config.TWITTER_URL) }
    settingsTraktIcon.onClick { openWebLink(Config.TRAKT_URL) }
    settingsTmdbIcon.onClick { openWebLink(Config.TMDB_URL) }
    settingsJustWatchIcon.onClick { openWebLink(Config.JUST_WATCH_URL) }
    settingsRoot.doOnApplyWindowInsets { view, insets, padding, _ ->
      val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      view.updatePadding(top = padding.top + inset)
    }
  }


  private fun render(uiState: SettingsUiState) {
    uiState.run {
      settings?.let {
        renderSettings(
          it,
          moviesEnabled,
          newsEnabled,
          streamingsEnabled
        )
      }
      renderLanguage(language)
      renderTheme(theme)
      country?.let { renderCountry(it) }
      dateFormat?.let { renderDateFormat(it, language) }
      progressNextType?.let { renderProgressType(it) }
      isPremium.let { isPremium ->
        settingsPremium.visibleIf(!isPremium && SHOW_PREMIUM)
        listOf(
          settingsTheme,
          settingsThemeValue,
          settingsNewsEnabled,
          settingsNewsEnabledSwitch,
        ).forEach {
          it.alpha = if (isPremium) 1F else 0.5F
        }

        listOf(
          settingsThemeValue,
          settingsNewsEnabledSwitch
        ).forEach {
          it.isEnabled = isPremium
        }

        listOf(
          settingsTheme,
          settingsNewsEnabled
        ).onClick {
          if (!isPremium) {
            val args = bundleOf()
            if (it.tag != null) {
              val feature = PremiumFeature.fromTag(requireContext(), it.tag.toString())
              feature?.let {
                args.putSerializable(ARG_ITEM, feature)
              }
            }
            navigateTo(R.id.actionSettingsFragmentToPremium, args)
          }
        }
      }
//      userId.let { settingsUserId.text = it }
      restartApp.let { if (it) restartApp() }
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

    settingsContactDevs.onClick {
      openMailMessage()
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

  private fun renderCountry(country: AppCountry) {
    settingsCountryValue.run {
      text = country.displayName
      onClick { showCountryDialog(country) }
    }
  }

  private fun renderProgressType(type: ProgressNextEpisodeType) {
    settingsProgressNextValue.run {
      text = when (type) {
        LAST_WATCHED -> getString(R.string.textNextEpisodeLastWatched).capitalizeWords()
        OLDEST -> getString(R.string.textNextEpisodeOldest).capitalizeWords()
      }
      onClick { showProgressTypeDialog(type) }
    }
  }

  private fun renderDateFormat(
    format: AppDateFormat,
    language: AppLanguage,
  ) {
    settingsDateFormat.run {
      settingsDateFormatValue.text = DateFormatProvider
        .loadSettingsFormat(format, language.code)
        .format(nowUtc().toLocalZone())
      onClick { showDateFormatDialog(format, language) }
    }
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
    val options = listOf(RECENTS)
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

  private fun showProgressTypeDialog(type: ProgressNextEpisodeType) {
    val options = ProgressNextEpisodeType.values()
    val displayOptions = options.map {
      val option = when (it) {
        LAST_WATCHED -> R.string.textNextEpisodeLastWatched
        OLDEST -> R.string.textNextEpisodeOldest
      }
      getString(option)
    }
    val selected = options.indexOf(type)

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(displayOptions.toTypedArray(), selected) { dialog, index ->
        if (index != selected) {
          viewModel.setProgressType(options[index])
        }
        dialog.dismiss()
      }
      .show()
  }

  private fun showDateFormatDialog(
    format: AppDateFormat,
    language: AppLanguage,
  ) {
    val options = AppDateFormat.values()
    val selected = options.indexOf(format)

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog_SmallText)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(
        options.map {
          DateFormatProvider.loadSettingsFormat(it, language.code).format(nowUtc().toLocalZone())
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

  private fun restartApp() {
    try {
      ProcessPhoenix.triggerRebirth(requireAppContext())
    } catch (error: Throwable) {
      Runtime.getRuntime().exit(0)
    }
  }

  private fun openWebLink(url: String) {
    openWebUrl(url) ?: showSnack(MessageEvent.Info(R.string.errorCouldNotFindApp))
  }

  private fun openMailMessage() {
    val id = "${settingsVersion.text}, ${settingsUserId.text}"
    val intent = Intent(ACTION_SENDTO).apply {
      data = Uri.parse("mailto:")
      putExtra(EXTRA_EMAIL, arrayOf(Config.DEVELOPER_MAIL))
      putExtra(EXTRA_SUBJECT, "Showly Message/Issue (Version: $id)")
    }
    if (intent.resolveActivity(requireActivity().packageManager) != null) {
      startActivity(intent)
    }
  }

  override fun onAuthorizationResult(authData: Uri?) {
    childFragmentManager.fragments.forEach {
      (it as? OnTraktAuthorizeListener)?.onAuthorizationResult(authData)
    }
  }
}
