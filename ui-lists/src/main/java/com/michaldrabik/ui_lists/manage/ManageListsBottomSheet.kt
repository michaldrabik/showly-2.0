package com.michaldrabik.ui_lists.manage

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.*
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.manage.di.UiManageListsComponentProvider
import com.michaldrabik.ui_lists.manage.recycler.ManageListsAdapter
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_TYPE
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_CREATE_LIST
import kotlinx.android.synthetic.main.fragment_lists.*
import kotlinx.android.synthetic.main.view_manage_lists.*
import kotlinx.android.synthetic.main.view_manage_lists.view.*

class ManageListsBottomSheet : BaseBottomSheetFragment<ManageListsViewModel>() {

  override val layoutResId = R.layout.view_manage_lists

  private val itemId by lazy { IdTrakt(requireArguments().getLong(ARG_ID, -1)) }
  private val itemType by lazy { requireArguments().getString(ARG_TYPE)!! }

  private var adapter: ManageListsAdapter? = null
  private var layoutManager: LinearLayoutManager? = null

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
    setupRecycler()
    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it) })
      loadLists(itemId, itemType)
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = ManageListsAdapter(
      itemCheckListener = { item, isChecked ->
        viewModel.onListItemChecked(itemId, itemType, item, isChecked)
      }
    )
    viewManageListsRecycler.apply {
      adapter = this@ManageListsBottomSheet.adapter
      layoutManager = this@ManageListsBottomSheet.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }
  }

  @SuppressLint("SetTextI18n")
  private fun setupView(view: View) {
    view.run {
      viewManageListsButton.onClick { findNavController().popBackStack() }
      viewManageListsCreateButton.onClick {
        setFragmentResultListener(REQUEST_CREATE_LIST) { _, _ -> viewModel.loadLists(itemId, itemType) }
        navigateTo(R.id.actionManageListsDialogToCreateListDialog, Bundle.EMPTY)
      }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiModel: ManageListsUiModel) {
    uiModel.run {
      items?.let {
        adapter?.setItems(it)
        viewManageListsEmptyView.visibleIf(it.isEmpty())
      }
    }
  }

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
