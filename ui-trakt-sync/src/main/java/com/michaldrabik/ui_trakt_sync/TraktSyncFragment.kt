package com.michaldrabik.ui_trakt_sync

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.common.extensions.dateFromMillis
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.data_remote.Config
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnTraktAuthorizeListener
import com.michaldrabik.ui_base.events.Event
import com.michaldrabik.ui_base.events.EventObserver
import com.michaldrabik.ui_base.trakt.TraktSyncService
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.openWebUrl
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.TraktSyncSchedule
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_trakt_sync.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TraktSyncFragment :
  BaseFragment<TraktSyncViewModel>(R.layout.fragment_trakt_sync),
  OnTraktAuthorizeListener,
  EventObserver {

  override val viewModel by viewModels<TraktSyncViewModel>()

  override fun onResume() {
    super.onResume()
    hideNavigation()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupStatusBar()

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          launch { messageState.collect { showSnack(it) } }
          invalidate()
        }
      }
    }
  }

  private fun setupView() {
    traktSyncToolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    traktSyncImportCheckbox.setOnCheckedChangeListener { _, isChecked ->
      traktSyncButton.isEnabled = (isChecked || traktSyncExportCheckbox.isChecked)
    }
    traktSyncExportCheckbox.setOnCheckedChangeListener { _, isChecked ->
      traktSyncButton.isEnabled = (isChecked || traktSyncImportCheckbox.isChecked)
    }
  }

  private fun setupStatusBar() {
    traktSyncRoot.doOnApplyWindowInsets { view, insets, _, _ ->
      view.updatePadding(top = insets.systemWindowInsetTop)
    }
  }

  private fun startImport() {
    val context = requireAppContext()
    TraktSyncService.createIntent(
      context,
      isImport = traktSyncImportCheckbox.isChecked,
      isExport = traktSyncExportCheckbox.isChecked
    ).run {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(this)
      } else {
        context.startService(this)
      }
    }
  }

  private fun checkScheduleImport(currentSchedule: TraktSyncSchedule?, quickSyncEnabled: Boolean?) {
    if (quickSyncEnabled == true && currentSchedule == TraktSyncSchedule.OFF) {
      MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
        .setTitle(R.string.textSettingsScheduleImportConfirmationTitle)
        .setMessage(R.string.textSettingsScheduleImportConfirmationMessage)
        .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
        .setPositiveButton(R.string.textYes) { _, _ -> scheduleImport(currentSchedule) }
        .setNegativeButton(R.string.textCancel) { _, _ -> }
        .show()
    } else {
      scheduleImport(currentSchedule)
    }
  }

  private fun scheduleImport(currentSchedule: TraktSyncSchedule?) {
    val options = TraktSyncSchedule.values()
    val optionsStrings = options.map { getString(it.stringRes) }.toTypedArray()
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(optionsStrings, options.indexOf(currentSchedule)) { dialog, index ->
        val schedule = options[index]
        viewModel.saveTraktSyncSchedule(schedule)
        showSnack(MessageEvent.info(schedule.confirmationStringRes))
        dialog.dismiss()
      }
      .show()
  }

  override fun onAuthorizationResult(authData: Uri?) {
    val code = authData?.getQueryParameter("code")
    viewModel.authorizeTrakt(code)
  }

  private fun render(uiState: TraktSyncUiState) {
    uiState.run {
      if (authError) findNavControl()?.popBackStack()
      isProgress.let {
        traktSyncButton.visibleIf(!it, false)
        traktSyncProgress.visibleIf(it)
        traktSyncImportCheckbox.visibleIf(!it)
        traktSyncExportCheckbox.visibleIf(!it)
        traktSyncScheduleButton.visibleIf(!it)
        traktLastSyncTimestamp.visibleIf(!it)
      }
      traktSyncStatus.text = progressStatus
      traktSyncScheduleButton.setText(traktSyncSchedule.buttonStringRes)

      if (lastTraktSyncTimestamp != 0L) {
        val date = dateFormat?.format(dateFromMillis(lastTraktSyncTimestamp).toLocalZone())?.capitalizeWords()
        traktLastSyncTimestamp.text = getString(R.string.textTraktSyncLastTimestamp, date)
      }

      if (isAuthorized) {
        traktSyncButton.text = getString(R.string.textTraktSyncStart)
        traktSyncButton.onClick { startImport() }
        traktSyncScheduleButton.onClick { checkScheduleImport(traktSyncSchedule, quickSyncEnabled) }
      } else {
        traktSyncButton.text = getString(R.string.textSettingsTraktAuthorizeTitle)
        traktSyncButton.onClick {
          openWebUrl(Config.TRAKT_AUTHORIZE_URL) ?: showSnack(MessageEvent.error(R.string.errorCouldNotFindApp))
        }
        traktSyncScheduleButton.gone()
      }
    }
  }

  override fun onNewEvent(event: Event) = viewModel.handleEvent(event)
}
