package com.michaldrabik.ui_base.common.sheets.remove_trakt_hidden

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.MessageEvent.Type
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_TYPE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.view_remove_trakt_hidden.*
import kotlinx.android.synthetic.main.view_remove_trakt_hidden.view.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RemoveTraktHiddenBottomSheet : BaseBottomSheetFragment<RemoveTraktHiddenViewModel>() {

  override val layoutResId = R.layout.view_remove_trakt_hidden

  private val itemId by lazy { requireArguments().getLong(ARG_ID) }
  private val itemType by lazy { requireArguments().getSerializable(ARG_TYPE) as Mode }

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    return inflater.cloneInContext(contextThemeWrapper).inflate(layoutResId, container, false)
  }

  override fun createViewModel() = ViewModelProvider(this)[RemoveTraktHiddenViewModel::class.java]

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView(view)

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
//          launch { uiState.collect { render(it) } }
          launch { messageState.collect { renderSnackbar(it) } }
        }
      }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun setupView(view: View) {
    view.run {
      viewRemoveTraktHiddenButtonNo.onClick { dismiss() }
      viewRemoveTraktHiddenButtonYes.onClick { dismiss() }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: RemoveTraktHiddenUiState) {
    uiState.run {
//      listDetails?.let {
//        viewCreateListNameValue.setText(it.name)
//        viewCreateListDescriptionValue.setText(it.description)
//      }
//      isLoading?.let {
//        viewCreateListNameInput.isEnabled = !it
//        viewCreateListDescriptionInput.isEnabled = !it
//        viewCreateListButton.isEnabled = !it
//        viewCreateListButton.setText(
//          when {
//            it -> R.string.textPleaseWait
//            !it && isEditMode() -> R.string.textEditList
//            else -> R.string.textCreateList
//          }
//        )
//      }
//      onListUpdated?.let {
//        it.consume()?.let {
//          setFragmentResult(REQUEST_CREATE_LIST, bundleOf())
//          dismiss()
//        }
//      }
    }
  }

  private fun renderSnackbar(message: MessageEvent) {
    message.consume()?.let {
      when (message.type) {
        Type.INFO -> viewRemoveTraktHiddenSnackHost.showInfoSnackbar(getString(it))
        Type.ERROR -> viewRemoveTraktHiddenSnackHost.showErrorSnackbar(getString(it))
      }
    }
  }
}
