package com.michaldrabik.ui_base.common.sheets.remove_trakt.remove_trakt_watchlist

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.databinding.ViewRemoveTraktWatchlistBinding
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
class RemoveTraktWatchlistBottomSheet : RemoveTraktBottomSheet<RemoveTraktWatchlistViewModel>(R.layout.view_remove_trakt_watchlist) {

  private val viewModel by viewModels<RemoveTraktWatchlistViewModel>()
  private val binding by viewBinding(ViewRemoveTraktWatchlistBinding::bind)

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
      viewRemoveTraktWatchlistButtonNo.onClick {
        setFragmentResult(REQUEST_REMOVE_TRAKT, bundleOf(RESULT to false))
        closeSheet()
      }
      viewRemoveTraktWatchlistButtonYes.onClick {
        viewModel.removeFromTrakt(itemIds, itemType)
      }
    }
  }

  private fun render(uiState: RemoveTraktWatchlistUiState) {
    uiState.run {
      isLoading?.let {
        with(binding) {
          viewRemoveTraktWatchlistProgress.visibleIf(it)
          viewRemoveTraktWatchlistButtonNo.visibleIf(!it, gone = false)
          viewRemoveTraktWatchlistButtonNo.isClickable = !it
          viewRemoveTraktWatchlistButtonYes.visibleIf(!it, gone = false)
          viewRemoveTraktWatchlistButtonYes.isClickable = !it
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
      is MessageEvent.Info -> binding.viewRemoveTraktWatchlistSnackHost.showInfoSnackbar(getString(message.textRestId))
      is MessageEvent.Error -> binding.viewRemoveTraktWatchlistSnackHost.showErrorSnackbar(getString(message.textRestId))
    }
  }
}
