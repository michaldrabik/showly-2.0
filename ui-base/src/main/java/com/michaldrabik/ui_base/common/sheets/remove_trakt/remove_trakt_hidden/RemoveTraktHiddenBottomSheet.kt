package com.michaldrabik.ui_base.common.sheets.remove_trakt.remove_trakt_hidden

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
import com.michaldrabik.ui_base.databinding.ViewRemoveTraktHiddenBinding
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
class RemoveTraktHiddenBottomSheet : RemoveTraktBottomSheet<RemoveTraktHiddenViewModel>() {

  override val layoutResId = R.layout.view_remove_trakt_hidden
  private val view by lazy { viewBinding as ViewRemoveTraktHiddenBinding }

  override fun createViewModel() = ViewModelProvider(this)[RemoveTraktHiddenViewModel::class.java]

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val view = super.onCreateView(inflater, container, savedInstanceState)
    return createViewBinding(ViewRemoveTraktHiddenBinding.bind(view))
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

    launchAndRepeatStarted(
      { viewModel.messageFlow.collect { renderSnackbar(it) } },
      { viewModel.uiState.collect { render(it) } }
    )
  }

  private fun setupView() {
    with(view) {
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
        with(view) {
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
    message.consume()?.let {
      when (message.type) {
        Type.INFO -> view.viewRemoveTraktHiddenSnackHost.showInfoSnackbar(getString(it))
        Type.ERROR -> view.viewRemoveTraktHiddenSnackHost.showErrorSnackbar(getString(it))
      }
    }
  }
}
