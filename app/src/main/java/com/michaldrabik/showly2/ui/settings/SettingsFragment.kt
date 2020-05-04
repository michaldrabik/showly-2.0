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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.michaldrabik.showly2.BuildConfig
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.Config.MY_SHOWS_RECENTS_OPTIONS
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.model.NotificationDelay
import com.michaldrabik.showly2.model.Settings
import com.michaldrabik.showly2.ui.common.OnTraktAuthorizeListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
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
      messageLiveData.observe(viewLifecycleOwner, Observer { showInfoSnackbar(it) })
      errorLiveData.observe(viewLifecycleOwner, Observer { showErrorSnackbar(it) })
      loadSettings()
    }
  }

  private fun setupView() {
    settingsToolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    settingsTraktSync.onClick {
      navigateTo(R.id.actionSettingsFragmentToTraktSync)
    }
    settingsDeleteCache.onClick { viewModel.deleteImagesCache() }
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
        settingsTraktAuthorizeIcon.visibleIf(isSignedIn)
        settingsTraktAuthorize.onClick {
          if (isSignedIn) viewModel.logoutTrakt()
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
    val applicationContext = requireContext().applicationContext

    settingsRecentShowsAmount.onClick {
      val options = MY_SHOWS_RECENTS_OPTIONS.map { it.toString() }.toTypedArray()
      val default = options.indexOf(settings.myShowsRecentsAmount.toString())
      AlertDialog.Builder(requireContext())
        .setSingleChoiceItems(options, default) { dialog, index ->
          viewModel.setRecentShowsAmount(options[index].toInt())
          dialog.dismiss()
        }
        .show()
    }

    settingsPushNotificationsSwitch
      .setCheckedSilent(settings.pushNotificationsEnabled) { _, isChecked ->
        viewModel.enablePushNotifications(isChecked)
      }

    settingsShowsNotificationsSwitch
      .setCheckedSilent(settings.episodesNotificationsEnabled) { _, isChecked ->
        viewModel.enableEpisodesAnnouncements(isChecked, applicationContext)
      }

    settingsDiscoverAnticipatedSwitch
      .setCheckedSilent(settings.showAnticipatedShows) { _, isChecked ->
        viewModel.enableAnticipatedShows(isChecked)
      }

    settingsWhenToNotifyValue.run {
      setText(settings.episodesNotificationsDelay.stringRes)
      onClick {
        val options = NotificationDelay.values()
        val default = options.indexOf(settings.episodesNotificationsDelay)
        AlertDialog.Builder(requireContext())
          .setSingleChoiceItems(options.map { getString(it.stringRes) }.toTypedArray(), default) { dialog, index ->
            viewModel.setWhenToNotify(options[index], applicationContext)
            dialog.dismiss()
          }
          .show()
      }
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

  override fun getSnackbarHost(): ViewGroup = settingsRoot
}
