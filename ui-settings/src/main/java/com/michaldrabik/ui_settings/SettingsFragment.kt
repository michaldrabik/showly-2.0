package com.michaldrabik.ui_settings

import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.ACTION_SENDTO
import android.content.Intent.EXTRA_EMAIL
import android.content.Intent.EXTRA_SUBJECT
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import com.michaldrabik.common.Config
import com.michaldrabik.common.Config.SHOW_PREMIUM
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnTraktAuthorizeListener
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.openWebUrl
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
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
      settings?.let { renderSettings() }
      isPremium.let { settingsPremium.visibleIf(!it && SHOW_PREMIUM) }
      userId.let { settingsUserId.text = it }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun renderSettings() {
    settingsContent.fadeIn(200)
    settingsContactDevs.onClick { openMailMessage() }
    settingsRateApp.onClick { PlayStoreHelper.openPlayStorePage(requireActivity()) }
    settingsVersion.text = "v${BuildConfig.VER_NAME} (${BuildConfig.VER_CODE})"
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
