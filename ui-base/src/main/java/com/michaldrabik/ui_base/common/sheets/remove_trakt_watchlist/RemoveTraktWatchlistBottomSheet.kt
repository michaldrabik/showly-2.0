package com.michaldrabik.ui_base.common.sheets.remove_trakt_watchlist

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.MessageEvent.Type
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_TYPE
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_REMOVE_TRAKT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.view_remove_trakt_watchlist.*
import kotlinx.android.synthetic.main.view_remove_trakt_watchlist.view.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class RemoveTraktWatchlistBottomSheet : BaseBottomSheetFragment<RemoveTraktWatchlistViewModel>() {

  override val layoutResId = R.layout.view_remove_trakt_watchlist

  private val itemId by lazy { requireArguments().getLong(ARG_ID) }
  private val itemType by lazy { requireArguments().getSerializable(ARG_TYPE) as Mode }

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    return inflater.cloneInContext(contextThemeWrapper).inflate(layoutResId, container, false)
  }

  override fun createViewModel() = ViewModelProvider(this)[RemoveTraktWatchlistViewModel::class.java]

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView(view)

    launchAndRepeatStarted(
      { viewModel.messageChannel.collect { renderSnackbar(it) } },
      { viewModel.uiState.collect { render(it) } }
    )
  }

  @SuppressLint("SetTextI18n")
  private fun setupView(view: View) {
    view.run {
      viewRemoveTraktWatchlistButtonNo.onClick { dismiss() }
      viewRemoveTraktWatchlistButtonYes.onClick {
        viewModel.removeFromTrakt(IdTrakt(itemId), itemType)
      }
    }
  }

  @SuppressLint("SetTextI18n")
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
          setFragmentResult(REQUEST_REMOVE_TRAKT, bundleOf())
          dismiss()
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
