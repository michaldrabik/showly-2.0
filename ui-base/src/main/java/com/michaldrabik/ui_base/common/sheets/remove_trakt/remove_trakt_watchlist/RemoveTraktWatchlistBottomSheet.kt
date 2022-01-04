package com.michaldrabik.ui_base.common.sheets.remove_trakt.remove_trakt_watchlist

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
import kotlinx.android.synthetic.main.view_remove_trakt_watchlist.*
import kotlinx.android.synthetic.main.view_remove_trakt_watchlist.view.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class RemoveTraktWatchlistBottomSheet : RemoveTraktBottomSheet<RemoveTraktWatchlistViewModel>() {

  override val layoutResId = R.layout.view_remove_trakt_watchlist

  override fun createViewModel() = ViewModelProvider(this)[RemoveTraktWatchlistViewModel::class.java]

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
        viewRemoveTraktWatchlistProgress.visibleIf(it)
        viewRemoveTraktWatchlistButtonNo.visibleIf(!it, gone = false)
        viewRemoveTraktWatchlistButtonNo.isClickable = !it
        viewRemoveTraktWatchlistButtonYes.visibleIf(!it, gone = false)
        viewRemoveTraktWatchlistButtonYes.isClickable = !it
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
        Type.INFO -> viewRemoveTraktWatchlistSnackHost.showInfoSnackbar(getString(it))
        Type.ERROR -> viewRemoveTraktWatchlistSnackHost.showErrorSnackbar(getString(it))
      }
    }
  }
}
