package com.michaldrabik.ui_trakt_sync

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.common.extensions.dateFromMillis
import com.michaldrabik.common.extensions.toDisplayString
import com.michaldrabik.common.extensions.toLocalTimeZone
import com.michaldrabik.network.Config
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnTraktAuthorizeListener
import com.michaldrabik.ui_base.events.Event
import com.michaldrabik.ui_base.events.EventObserver
import com.michaldrabik.ui_base.trakt.TraktSyncService
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.TraktSyncSchedule
import com.michaldrabik.ui_trakt_sync.di.UiTraktSyncComponentProvider
import kotlinx.android.synthetic.main.fragment_trakt_sync.*

class TraktSyncFragment :
  BaseFragment<TraktSyncViewModel>(R.layout.fragment_trakt_sync),
  OnTraktAuthorizeListener,
  EventObserver {

  override val viewModel by viewModels<TraktSyncViewModel> { viewModelFactory }

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiTraktSyncComponentProvider).provideTraktSyncComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupStatusBar()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
      messageLiveData.observe(viewLifecycleOwner, { showSnack(it) })
      invalidate()
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

  override fun onResume() {
    super.onResume()
    handleBackPressed()
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
        viewModel.saveTraktSyncSchedule(schedule, requireAppContext())
        showSnack(MessageEvent.info(schedule.confirmationStringRes))
        dialog.dismiss()
      }
      .show()
  }

  private fun startAuthorization() {
    Intent(Intent.ACTION_VIEW).run {
      data = Uri.parse(Config.TRAKT_AUTHORIZE_URL)
      startActivity(this)
    }
  }

  override fun onAuthorizationResult(authData: Uri?) = viewModel.authorizeTrakt(authData)

  private fun render(uiModel: TraktSyncUiModel) {
    uiModel.run {
      isProgress?.let {
        traktSyncButton.visibleIf(!it, false)
        traktSyncProgress.visibleIf(it)
        traktSyncImportCheckbox.visibleIf(!it)
        traktSyncExportCheckbox.visibleIf(!it)
        traktSyncScheduleButton.visibleIf(!it)
        traktLastSyncTimestamp.visibleIf(!it)
      }
      progressStatus?.let { traktSyncStatus.text = it }
      authError?.let { findNavControl().popBackStack() }
      traktSyncSchedule?.let { traktSyncScheduleButton.setText(it.buttonStringRes) }
      lastTraktSyncTimestamp?.let {
        if (it != 0L) {
          val date = dateFromMillis(it).toLocalTimeZone().toDisplayString()
          traktLastSyncTimestamp.text = getString(R.string.textTraktSyncLastTimestamp, date)
        }
      }
      isAuthorized?.let {
        when {
          it -> {
            traktSyncButton.text = getString(R.string.textTraktSyncStart)
            traktSyncButton.onClick { startImport() }
            traktSyncScheduleButton.onClick { checkScheduleImport(traktSyncSchedule, quickSyncEnabled) }
          }
          else -> {
            traktSyncButton.text = getString(R.string.textSettingsTraktAuthorizeTitle)
            traktSyncButton.onClick { startAuthorization() }
            traktSyncScheduleButton.gone()
          }
        }
      }
    }
  }

  private fun handleBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      remove()
      findNavControl().popBackStack()
    }
  }

  override fun getSnackbarHost(): ViewGroup = traktSyncRoot

  override fun onNewEvent(event: Event) = viewModel.handleEvent(event)
}
