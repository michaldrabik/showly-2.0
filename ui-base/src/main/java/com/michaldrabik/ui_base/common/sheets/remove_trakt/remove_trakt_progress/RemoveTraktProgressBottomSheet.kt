package com.michaldrabik.ui_base.common.sheets.remove_trakt.remove_trakt_progress

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.databinding.ViewRemoveTraktProgressBinding
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
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class RemoveTraktProgressBottomSheet : RemoveTraktBottomSheet<RemoveTraktProgressViewModel>() {

  override val layoutResId = R.layout.view_remove_trakt_progress
  private val view by lazy { viewBinding as ViewRemoveTraktProgressBinding }

  override fun createViewModel() = ViewModelProvider(this)[RemoveTraktProgressViewModel::class.java]

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val view = super.onCreateView(inflater, container, savedInstanceState)
    return createViewBinding(ViewRemoveTraktProgressBinding.bind(view))
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

    launchAndRepeatStarted(
      { viewModel.messageChannel.collect { renderSnackbar(it) } },
      { viewModel.uiState.collect { render(it) } }
    )
  }

  private fun setupView() {
    with(view) {
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
        with(view) {
          viewRemoveTraktProgressProgress.visibleIf(it)
          viewRemoveTraktProgressButtonNo.visibleIf(!it, gone = false)
          viewRemoveTraktProgressButtonNo.isClickable = !it
          viewRemoveTraktProgressButtonYes.visibleIf(!it, gone = false)
          viewRemoveTraktProgressButtonYes.isClickable = !it
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
    message.consume()?.let {
      when (message.type) {
        Type.INFO -> view.viewRemoveTraktProgressSnackHost.showInfoSnackbar(getString(it))
        Type.ERROR -> view.viewRemoveTraktProgressSnackHost.showErrorSnackbar(getString(it))
      }
    }
  }
}
