package com.michaldrabik.showly2.ui.settings

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.michaldrabik.showly2.Config.MY_SHOWS_RECENTS_OPTIONS
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.main.MainActivity
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.setCheckedSilent
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : BaseFragment<SettingsViewModel>() {

  override val layoutResId = R.layout.fragment_settings

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun createViewModel(provider: ViewModelProvider) =
    provider.get(SettingsViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
    viewModel.loadSettings()

    settingsToolbar.setNavigationOnClickListener { activity?.onBackPressed() }
  }

  private fun render(uiModel: SettingsUiModel) {
    uiModel.settings?.let { settings ->
      settingsRecentShowsAmount.onClick {
        val options = MY_SHOWS_RECENTS_OPTIONS.map { it.toString() }.toTypedArray()
        val default = settings.myShowsRecentsAmount
        AlertDialog.Builder(requireContext())
          .setSingleChoiceItems(options, options.indexOf(default.toString())) { dialog, index ->
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
          viewModel.enableShowsNotifications(isChecked, requireContext().applicationContext)
        }
    }
  }

  override fun onResume() {
    super.onResume()
    handleBackPressed()
  }

  private fun handleBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      remove()
      (activity as MainActivity).showNavigation()
      findNavController().popBackStack()
    }
  }
}
