package com.michaldrabik.ui_base.common.sheets.remove_trakt.remove_trakt_hidden

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.databinding.ViewRemoveTraktHiddenBinding
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_REMOVE_TRAKT
import com.michaldrabik.ui_navigation.java.NavigationArgs.RESULT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RemoveTraktHiddenBottomSheet : RemoveTraktBottomSheet<RemoveTraktHiddenViewModel>(R.layout.view_remove_trakt_hidden) {

  private val viewModel by viewModels<RemoveTraktHiddenViewModel>()
  private val binding by viewBinding(ViewRemoveTraktHiddenBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

    launchAndRepeatStarted(
      { viewModel.messageFlow.collect { renderSnackbar(it) } },
      { viewModel.uiState.collect { render(it) } }
    )
  }

  private fun setupView() {
    with(binding) {
      viewRemoveTraktHiddenButtonNo.onClick {
        setFragmentResult(REQUEST_REMOVE_TRAKT, bundleOf(RESULT to false))
        closeSheet()
      }
      viewRemoveTraktHiddenButtonYes.onClick {
        viewModel.removeFromTrakt(itemIds, itemType)
      }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: RemoveTraktHiddenUiState) {
    uiState.run {
      isLoading?.let {
        with(binding) {
          viewRemoveTraktHiddenProgress.visibleIf(it)
          viewRemoveTraktHiddenButtonNo.visibleIf(!it, gone = false)
          viewRemoveTraktHiddenButtonNo.isClickable = !it
          viewRemoveTraktHiddenButtonYes.visibleIf(!it, gone = false)
          viewRemoveTraktHiddenButtonYes.isClickable = !it
        }
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
    when (message) {
      is MessageEvent.Info -> binding.viewRemoveTraktHiddenSnackHost.showInfoSnackbar(getString(message.textRestId))
      is MessageEvent.Error -> binding.viewRemoveTraktHiddenSnackHost.showErrorSnackbar(getString(message.textRestId))
    }
  }
}
