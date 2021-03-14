package com.michaldrabik.ui_lists.create

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.shake
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.create.di.UiCreateListComponentProvider
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_CREATE_LIST
import kotlinx.android.synthetic.main.view_create_list.*
import kotlinx.android.synthetic.main.view_create_list.view.*

class CreateListBottomSheet : BaseBottomSheetFragment<CreateListViewModel>() {

  override val layoutResId = R.layout.view_create_list

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiCreateListComponentProvider).provideCreateListComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    return inflater.cloneInContext(contextThemeWrapper).inflate(layoutResId, container, false)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(CreateListViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView(view)
    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it) })
    }
  }

  @SuppressLint("SetTextI18n")
  private fun setupView(view: View) {
    view.run {
      viewCreateListButton.onClick { onCreateListClick() }
    }
  }

  private fun onCreateListClick() {
    val name = viewCreateListNameValue.text?.toString() ?: ""
    val description = viewCreateListDescriptionValue.text?.toString()
    if (name.trim().isBlank()) {
      viewCreateListNameInput.shake()
      return
    }
    viewModel.createList(name, description)
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiModel: CreateListUiModel) {
    uiModel.run {
      isLoading?.let {
        viewCreateListButton.isEnabled == !it
      }
      listCreatedEvent?.let {
        it.consume()?.let {
          setFragmentResult(REQUEST_CREATE_LIST, bundleOf())
          dismiss()
        }
      }
    }
  }
}
