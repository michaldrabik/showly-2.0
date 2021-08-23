package com.michaldrabik.ui_lists.create

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.MessageEvent.Type
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.shake
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_LIST
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_CREATE_LIST
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.view_create_list.*
import kotlinx.android.synthetic.main.view_create_list.view.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateListBottomSheet : BaseBottomSheetFragment<CreateListViewModel>() {

  override val layoutResId = R.layout.view_create_list

  private val list by lazy { requireArguments().getParcelable<CustomList>(ARG_LIST) }

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    return inflater.cloneInContext(contextThemeWrapper).inflate(layoutResId, container, false)
  }

  override fun createViewModel() =
    ViewModelProvider(this).get(CreateListViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView(view)

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          launch { messageState.collect { renderSnackbar(it) } }
          if (isEditMode()) {
            viewModel.loadDetails(list?.id!!)
          }
        }
      }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun setupView(view: View) {
    view.run {
      viewCreateListButton.onClick { onCreateListClick() }
      if (isEditMode()) {
        viewCreateListTitle.setText(R.string.textEditList)
        viewCreateListSubtitle.setText(R.string.textEditListDescription)
        viewCreateListButton.setText(R.string.textApply)
      }
    }
  }

  private fun onCreateListClick() {
    val name = viewCreateListNameValue.text?.toString() ?: ""
    val description = viewCreateListDescriptionValue.text?.toString()
    if (name.trim().isBlank()) {
      viewCreateListNameInput.shake()
      return
    }
    if (isEditMode()) {
      viewModel.updateList(list!!.copy(name = name, description = description))
    } else {
      viewModel.createList(name, description)
    }
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: CreateListUiState) {
    uiState.run {
      listDetails?.let {
        viewCreateListNameValue.setText(it.name)
        viewCreateListDescriptionValue.setText(it.description)
      }
      isLoading?.let {
        viewCreateListNameInput.isEnabled = !it
        viewCreateListDescriptionInput.isEnabled = !it
        viewCreateListButton.isEnabled = !it
        viewCreateListButton.setText(
          when {
            it -> R.string.textPleaseWait
            !it && isEditMode() -> R.string.textEditList
            else -> R.string.textCreateList
          }
        )
      }
      onListUpdated?.let {
        it.consume()?.let {
          setFragmentResult(REQUEST_CREATE_LIST, bundleOf())
          dismiss()
        }
      }
    }
  }

  private fun renderSnackbar(message: MessageEvent) {
    message.consume()?.let {
      when (message.type) {
        Type.INFO -> viewCreateListSnackHost.showInfoSnackbar(getString(it))
        Type.ERROR -> viewCreateListSnackHost.showErrorSnackbar(getString(it))
      }
    }
  }

  private fun isEditMode() = list != null
}
