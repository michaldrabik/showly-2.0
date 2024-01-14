package com.michaldrabik.ui_lists.create

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.optionalParcelable
import com.michaldrabik.ui_base.utilities.extensions.shake
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.databinding.ViewCreateListBinding
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_LIST
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_CREATE_LIST
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateListBottomSheet : BaseBottomSheetFragment(R.layout.view_create_list) {

  private val viewModel by viewModels<CreateListViewModel>()
  private val binding by viewBinding(ViewCreateListBinding::bind)

  private val list: CustomList? by lazy { optionalParcelable(ARG_LIST) }

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          launch { messageFlow.collect { renderSnackbar(it) } }
          if (isEditMode()) {
            viewModel.loadDetails(list?.id!!)
          }
        }
      }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun setupView() {
    with(binding) {
      viewCreateListButton.onClick { onCreateListClick() }
      if (isEditMode()) {
        viewCreateListTitle.setText(R.string.textEditList)
        viewCreateListSubtitle.setText(R.string.textEditListDescription)
        viewCreateListButton.setText(R.string.textApply)
      }
    }
  }

  private fun onCreateListClick() {
    val name = binding.viewCreateListNameValue.text?.toString() ?: ""
    val description = binding.viewCreateListDescriptionValue.text?.toString()
    if (name.trim().isBlank()) {
      binding.viewCreateListNameInput.shake()
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
        binding.viewCreateListNameValue.setText(it.name)
        binding.viewCreateListDescriptionValue.setText(it.description)
      }
      isLoading?.let {
        with(binding) {
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
      }
      onListUpdated?.let {
        it.consume()?.let {
          setFragmentResult(REQUEST_CREATE_LIST, bundleOf())
          closeSheet()
        }
      }
    }
  }

  private fun renderSnackbar(message: MessageEvent) {
    when (message) {
      is MessageEvent.Info -> binding.viewCreateListSnackHost.showInfoSnackbar(getString(message.textRestId))
      is MessageEvent.Error -> binding.viewCreateListSnackHost.showErrorSnackbar(getString(message.textRestId))
    }
  }

  private fun isEditMode() = list != null
}
