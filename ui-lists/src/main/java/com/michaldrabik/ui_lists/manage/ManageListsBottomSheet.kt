package com.michaldrabik.ui_lists.manage

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.events.Event
import com.michaldrabik.ui_base.events.EventObserver
import com.michaldrabik.ui_base.events.TraktQuickSyncSuccess
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.manage.recycler.ManageListsAdapter
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_TYPE
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_CREATE_LIST
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_MANAGE_LISTS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_lists.*
import kotlinx.android.synthetic.main.view_manage_lists.*
import kotlinx.android.synthetic.main.view_manage_lists.view.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageListsBottomSheet : BaseBottomSheetFragment<ManageListsViewModel>(), EventObserver {

  override val layoutResId = R.layout.view_manage_lists

  private val itemId by lazy { IdTrakt(requireArguments().getLong(ARG_ID, -1)) }
  private val itemType by lazy { requireArguments().getString(ARG_TYPE)!! }

  private var adapter: ManageListsAdapter? = null
  private var layoutManager: LinearLayoutManager? = null

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    return inflater.cloneInContext(contextThemeWrapper).inflate(layoutResId, container, false)
  }

  override fun createViewModel() =
    ViewModelProvider(this).get(ManageListsViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          loadLists(itemId, itemType)
        }
      }
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = ManageListsAdapter(
      itemCheckListener = { item, isChecked ->
        val context = requireContext().applicationContext
        viewModel.onListItemChecked(context, itemId, itemType, item, isChecked)
      }
    )
    viewManageListsRecycler.apply {
      adapter = this@ManageListsBottomSheet.adapter
      layoutManager = this@ManageListsBottomSheet.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }
  }

  private fun setupView() {
    viewManageListsButton.onClick { findNavController().popBackStack() }
    viewManageListsCreateButton.onClick {
      setFragmentResultListener(REQUEST_CREATE_LIST) { _, _ -> viewModel.loadLists(itemId, itemType) }
      navigateTo(R.id.actionManageListsDialogToCreateListDialog, Bundle.EMPTY)
    }
    if (itemType == Mode.MOVIES.type) {
      viewManageListsSubtitle.setText(R.string.textManageListsMovies)
    }
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: ManageListsUiState) {
    uiState.run {
      items?.let {
        adapter?.setItems(it)
        viewManageListsEmptyView.visibleIf(it.isEmpty())
      }
    }
  }

  override fun onDestroyView() {
    setFragmentResult(REQUEST_MANAGE_LISTS, bundleOf())
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }

  override fun onNewEvent(event: Event) {
    activity?.runOnUiThread {
      if (event is TraktQuickSyncSuccess) {
        val text = resources.getQuantityString(R.plurals.textTraktQuickSyncComplete, event.count, event.count)
        viewManageListsSnackHost?.showInfoSnackbar(text)
      }
    }
  }
}
