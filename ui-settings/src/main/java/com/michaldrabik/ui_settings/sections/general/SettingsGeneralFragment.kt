package com.michaldrabik.ui_settings.sections.general

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.processphoenix.ProcessPhoenix
import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_base.dates.AppDateFormat
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.PremiumFeature
import com.michaldrabik.ui_model.ProgressDateSelectionType
import com.michaldrabik.ui_model.ProgressDateSelectionType.ALWAYS_ASK
import com.michaldrabik.ui_model.ProgressDateSelectionType.NOW
import com.michaldrabik.ui_model.ProgressNextEpisodeType
import com.michaldrabik.ui_model.ProgressNextEpisodeType.LAST_WATCHED
import com.michaldrabik.ui_model.ProgressNextEpisodeType.OLDEST
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ITEM
import com.michaldrabik.ui_settings.R
import com.michaldrabik.ui_settings.databinding.FragmentSettingsGeneralBinding
import com.michaldrabik.ui_settings.helpers.AppLanguage
import com.michaldrabik.ui_settings.helpers.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsGeneralFragment : BaseFragment<SettingsGeneralViewModel>(R.layout.fragment_settings_general) {

  override val viewModel by viewModels<SettingsGeneralViewModel>()
  private val binding by viewBinding(FragmentSettingsGeneralBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      doAfterLaunch = { viewModel.loadSettings() }
    )
  }

  private fun setupView() {
    with(binding) {
      settingsTabletColumns.visibleIf(isTablet)

      settingsIncludeSpecials.onClick {
        viewModel.enableSpecialSeasons(!settingsIncludeSpecialsSwitch.isChecked)
      }
      settingsMoviesEnabled.onClick {
        viewModel.enableMovies(!settingsMoviesEnabledSwitch.isChecked)
      }
      settingsStreamingsEnabled.onClick {
        viewModel.enableStreamings(!settingsStreamingsEnabledSwitch.isChecked)
      }
    }
  }

  private fun render(uiState: SettingsGeneralUiState) {
    with(binding) {
      with(uiState) {
        settingsRecentShowsAmount.onClick { showRecentShowsDialog(settings) }

        settingsMoviesEnabledSwitch.isChecked = moviesEnabled
        settingsStreamingsEnabledSwitch.isChecked = streamingsEnabled

        renderSettings(settings)
        renderLanguage(language)
        renderTheme(theme)
        renderCountry(country)
        renderProgressType(progressNextType)
        renderDateSelection(progressDateSelectionType)
        renderProgressUpcoming(progressUpcomingDays)
        renderDateFormat(dateFormat, language)
        renderTabletColumns(tabletColumns)

        settingsTheme.alpha = if (isPremium) 1F else 0.5F
        if (isPremium) {
          settingsThemeTitle.setCompoundDrawables(null, null, null, null)
        }

        if (restartApp) restartApp()
      }
    }
  }

  private fun renderSettings(settings: Settings?) {
    if (settings == null) return
    with(binding) {
      settingsIncludeSpecialsSwitch.isChecked = settings.specialSeasonsEnabled
    }
  }

  private fun renderLanguage(language: AppLanguage) {
    with(binding) {
      settingsLanguageValue.setText(language.displayName)
      settingsLanguage.onClick { showLanguageDialog(language) }
    }
  }

  private fun renderTheme(theme: AppTheme) {
    with(binding) {
      settingsThemeValue.setText(theme.displayName)
      settingsTheme.onClick {
        onPremiumAction(tag)
      }
    }
  }

  private fun renderTabletColumns(columns: Int) {
    with(binding) {
      settingsTabletColumnsValue.text = columns.toString()
      settingsTabletColumns.onClick {
        showTabletColumnsDialog(columns)
      }
    }
  }

  private fun renderCountry(country: AppCountry?) {
    if (country == null) return
    with(binding) {
      settingsCountryValue.setText(country.displayName)
      settingsCountry.onClick { showCountryDialog(country) }
    }
  }

  private fun renderProgressUpcoming(progressUpcomingDays: Long?) {
    if (progressUpcomingDays == null) return
    with(binding) {
      settingsUpcomingValue.text = if (progressUpcomingDays > 0L) {
        getString(R.string.textDays, progressUpcomingDays)
      } else {
        getString(R.string.textDisabled)
      }
      settingsUpcomingSection.onClick {
        showProgressUpcomingDialog(progressUpcomingDays)
      }
    }
  }

  private fun renderProgressType(type: ProgressNextEpisodeType?) {
    if (type == null) return
    with(binding) {
      settingsProgressNextValue.text = when (type) {
        LAST_WATCHED -> getString(R.string.textNextEpisodeLastWatched)
        OLDEST -> getString(R.string.textNextEpisodeOldest)
      }
      settingsProgressNext.onClick { showProgressTypeDialog(type) }
    }
  }

  private fun renderDateSelection(type: ProgressDateSelectionType?) {
    if (type == null) return
    with(binding) {
      settingsDateSelectionValue.text = when (type) {
        ALWAYS_ASK -> getString(R.string.textDateSelectionAsk)
        NOW -> getString(R.string.textDateSelectionNow)
      }
      settingsDateSelection.onClick { showDateSelectionTypeDialog(type) }
    }
  }

  private fun renderDateFormat(
    format: AppDateFormat?,
    language: AppLanguage,
  ) {
    if (format == null) return
    with(binding) {
      settingsDateFormatValue.text = DateFormatProvider
        .loadSettingsFormat(format, language.code)
        .format(nowUtc().toLocalZone())
      settingsDateFormat.onClick { showDateFormatDialog(format, language) }
    }
  }

  private fun onPremiumAction(tag: Any?) {
    val args = bundleOf()
    if (tag != null) {
      val feature = PremiumFeature.fromTag(requireContext(), tag.toString())
      feature?.let {
        args.putSerializable(ARG_ITEM, feature)
      }
    }
    navigateTo(R.id.actionSettingsFragmentToPremium, args)
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

  private fun showProgressUpcomingDialog(days: Long) {
    val options = Config.PROGRESS_UPCOMING_OPTIONS
    val selected = options.indexOfFirst { it.toLong() == days }

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(
        options.map { if (it == 0) getString(R.string.textDisabled) else getString(R.string.textDays, it) }.toTypedArray(),
        selected
      ) { dialog, index ->
        if (index != selected) {
          viewModel.setProgressUpcomingDays(options[index].toLong())
        }
        dialog.dismiss()
      }
      .show()
  }

  private fun showTabletColumnsDialog(columns: Int) {
    val options = arrayOf(1, 2)
    val selected = options.indexOf(columns)
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(options.map { it.toString() }.toTypedArray(), selected) { dialog, index ->
        if (index != selected) {
          viewModel.setTabletColumns(options[index])
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
      .setSingleChoiceItems(options.map { getString(it.displayName) }.toTypedArray(), selected) { dialog, index ->
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

  private fun showDateSelectionTypeDialog(type: ProgressDateSelectionType) {
    val options = ProgressDateSelectionType.values()
    val displayOptions = options.map {
      val option = when (it) {
        ALWAYS_ASK -> R.string.textDateSelectionAsk
        NOW -> R.string.textDateSelectionNow
      }
      getString(option)
    }
    val selected = options.indexOf(type)

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(displayOptions.toTypedArray(), selected) { dialog, index ->
        if (index != selected) {
          viewModel.setDateSelectionType(options[index])
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

  private fun showRecentShowsDialog(settings: Settings?) {
    if (settings == null) return

    val options = Config.MY_SHOWS_RECENTS_OPTIONS.map { it.toString() }.toTypedArray()
    val default = options.indexOf(settings.myRecentsAmount.toString())

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(options, default) { dialog, index ->
        viewModel.setRecentShowsAmount(options[index].toInt())
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
}
