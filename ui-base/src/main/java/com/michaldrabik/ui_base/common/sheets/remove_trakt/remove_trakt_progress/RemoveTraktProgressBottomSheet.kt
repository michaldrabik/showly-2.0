package com.michaldrabik.ui_base.common.sheets.remove_trakt.remove_trakt_progress

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.MessageEvent.Type
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_REMOVE_TRAKT
import com.michaldrabik.ui_navigation.java.NavigationArgs.RESULT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.view_remove_trakt_progress.*
import kotlinx.android.synthetic.main.view_remove_trakt_progress.view.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class RemoveTraktProgressBottomSheet : RemoveTraktBottomSheet<RemoveTraktProgressViewModel>() {

  override val layoutResId = R.layout.view_remove_trakt_progress

  override fun createViewModel() = ViewModelProvider(this)[RemoveTraktProgressViewModel::class.java]

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView(view)

    launchAndRepeatStarted(
      { viewModel.messageChannel.collect { renderSnackbar(it) } },
      { viewModel.uiState.collect { render(it) } }
    )
  }

  private fun setupView(view: View) {
    view.run {
      viewRemoveTraktProgressButtonNo.onClick {
        setFragmentResult(REQUEST_REMOVE_TRAKT, bundleOf(RESULT to false))
        closeSheet()
      }
      viewRemoveTraktProgressButtonYes.onClick {
        viewModel.removeFromTrakt(itemIds, itemType)
      }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: RemoveTraktProgressUiState) {
    uiState.run {
      isLoading?.let {
        viewRemoveTraktProgressProgress.visibleIf(it)
        viewRemoveTraktProgressButtonNo.visibleIf(!it, gone = false)
        viewRemoveTraktProgressButtonNo.isClickable = !it
        viewRemoveTraktProgressButtonYes.visibleIf(!it, gone = false)
        viewRemoveTraktProgressButtonYes.isClickable = !it
      }
      isFinished?.let {
        if (it) {
          setFragmentResult(REQUEST_REMOVE_TRAKT, bundleOf(RESULT to true))
          closeSheet()
        }
      }
    }
  }

  private fun renderSnackbar(message: MessageEvent) {
    message.consume()?.let {
      when (message.type) {
        Type.INFO -> viewRemoveTraktProgressSnackHost.showInfoSnackbar(getString(it))
        Type.ERROR -> viewRemoveTraktProgressSnackHost.showErrorSnackbar(getString(it))
      }
    }
  }
}
