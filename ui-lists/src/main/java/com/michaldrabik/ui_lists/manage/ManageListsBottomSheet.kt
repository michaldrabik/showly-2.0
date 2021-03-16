package com.michaldrabik.ui_lists.manage

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.manage.di.UiManageListsComponentProvider

class ManageListsBottomSheet : BaseBottomSheetFragment<ManageListsViewModel>() {

  override val layoutResId = R.layout.view_manage_lists

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiManageListsComponentProvider).provideManageListsComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    return inflater.cloneInContext(contextThemeWrapper).inflate(layoutResId, container, false)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(ManageListsViewModel::class.java)

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
    }
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiModel: ManageListsUiModel) {
    uiModel.run {
    }
  }
}
