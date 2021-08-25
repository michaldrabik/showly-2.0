package com.michaldrabik.ui_lists.details

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.add
import com.michaldrabik.ui_base.utilities.extensions.disableUi
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.enableUi
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.details.helpers.ListItemDragListener
import com.michaldrabik.ui_lists.details.helpers.ListItemSwipeListener
import com.michaldrabik.ui_lists.details.helpers.ReorderListCallback
import com.michaldrabik.ui_lists.details.helpers.ReorderListCallbackAdapter
import com.michaldrabik.ui_lists.details.recycler.ListDetailsAdapter
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import com.michaldrabik.ui_lists.details.views.ListDetailsDeleteConfirmView
import com.michaldrabik.ui_lists.details.views.ListDetailsFiltersView
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_model.SortOrderList
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_LIST
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_list_details.*
import kotlinx.android.synthetic.main.fragment_lists.*
import kotlinx.android.synthetic.main.view_list_delete_confirm.view.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListDetailsFragment :
  BaseFragment<ListDetailsViewModel>(R.layout.fragment_list_details), ListItemDragListener, ListItemSwipeListener {

  override val viewModel by viewModels<ListDetailsViewModel>()

  private val list by lazy { requireArguments().getParcelable<CustomList>(ARG_LIST)!! }

  private var adapter: ListDetailsAdapter? = null
  private var touchHelper: ItemTouchHelper? = null
  private var layoutManager: LinearLayoutManager? = null

  private var isReorderMode = false

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          launch { messageState.collect { showSnack(it) } }
          loadDetails(list.id)
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    hideNavigation()
  }

  override fun onPause() {
    enableUi()
    super.onPause()
  }

  private fun setupView() {
    fragmentListDetailsRoot.doOnApplyWindowInsets { view, insets, padding, _ ->
      view.updatePadding(top = padding.top + insets.systemWindowInsetTop)
    }
    with(fragmentListDetailsToolbar) {
      title = list.name
      subtitle = list.description
      setNavigationOnClickListener {
        if (isReorderMode) toggleReorderMode()
        else activity?.onBackPressed()
      }
    }
    fragmentListDetailsManageButton.onClick { toggleReorderMode() }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = ListDetailsAdapter(
      itemClickListener = { openItemDetails(it) },
      missingImageListener = { item: ListDetailsItem, force: Boolean ->
        viewModel.loadMissingImage(item, force)
      },
      missingTranslationListener = {
        viewModel.loadMissingTranslation(it)
      },
      itemsChangedListener = {
        fragmentListDetailsRecycler.scrollToPosition(0)
      },
      itemsClearedListener = {
        if (isReorderMode) viewModel.updateRanks(list.id, it)
      },
      itemsSwipedListener = {
        viewModel.deleteListItem(requireAppContext(), list.id, it)
      },
      itemDragStartListener = this,
      itemSwipeStartListener = this
    ).apply {
      stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }
    fragmentListDetailsRecycler.apply {
      adapter = this@ListDetailsFragment.adapter
      layoutManager = this@ListDetailsFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
    val touchCallback = ReorderListCallback(adapter as ReorderListCallbackAdapter)
    touchHelper = ItemTouchHelper(touchCallback)
    touchHelper?.attachToRecyclerView(fragmentListDetailsRecycler)
  }

  override fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      if (isReorderMode) {
        toggleReorderMode()
      } else {
        isEnabled = false
        findNavControl()?.popBackStack()
      }
    }
  }

  private fun showSortOrderDialog(order: SortOrderList, types: List<Mode>) {
    val view = ListDetailsFiltersView(requireContext()).apply {
      setTypes(types)
      onChipsChangeListener = { types ->
        viewModel.setSortTypes(list.id, types)
      }
    }

    val options = SortOrderList.values()
    val optionsStrings = options.map { getString(it.displayString) }.toTypedArray()

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setTitle(R.string.textSortBy)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(optionsStrings, options.indexOf(order)) { dialog, index ->
        viewModel.setSortOrder(list.id, options[index])
        dialog.dismiss()
      }
      .setView(view)
      .show()
  }

  private fun showDeleteDialog(quickRemoveEnabled: Boolean) {
    val view = ListDetailsDeleteConfirmView(requireContext())
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .apply { if (quickRemoveEnabled) setView(view) }
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setTitle(R.string.textConfirmDeleteListTitle)
      .setMessage(R.string.textConfirmDeleteListSubtitle)
      .setPositiveButton(R.string.textYes) { _, _ ->
        val removeFromTrakt = view.viewListDeleteConfirmCheckbox?.isChecked
        viewModel.deleteList(list.id, removeFromTrakt == true)
      }
      .setNegativeButton(R.string.textNo) { _, _ -> }
      .show()
  }

  private fun showEditDialog() {
    setFragmentResultListener(NavigationArgs.REQUEST_CREATE_LIST) { _, _ ->
      viewModel.loadDetails(list.id)
    }
    val bundle = bundleOf(ARG_LIST to list)
    navigateTo(R.id.actionListDetailsFragmentToEditListDialog, bundle)
  }

  private fun openItemDetails(listItem: ListDetailsItem) {
    disableUi()
    fragmentListDetailsRoot.fadeOut(150) {
      val bundle = bundleOf(
        ARG_SHOW_ID to listItem.show?.traktId,
        ARG_MOVIE_ID to listItem.movie?.traktId
      )
      val destination =
        when {
          listItem.isShow() -> R.id.actionListDetailsFragmentToShowDetailsFragment
          listItem.isMovie() -> R.id.actionListDetailsFragmentToMovieDetailsFragment
          else -> throw IllegalStateException()
        }
      navigateTo(destination, bundle)
    }.add(animations)
  }

  private fun openPopupMenu(quickRemoveEnabled: Boolean) {
    PopupMenu(requireContext(), fragmentListDetailsMoreButton, Gravity.CENTER).apply {
      inflate(R.menu.menu_list_details)
      setOnMenuItemClickListener { menuItem ->
        when (menuItem.itemId) {
          R.id.menuListDetailsEdit -> showEditDialog()
          R.id.menuListDetailsDelete -> showDeleteDialog(quickRemoveEnabled)
        }
        true
      }
      show()
    }
  }

  private fun toggleReorderMode() {
    isReorderMode = !isReorderMode
    viewModel.setReorderMode(list.id, isReorderMode)
  }

  private fun render(uiState: ListDetailsUiState) {
    uiState.run {
      listDetails?.let { details ->
        with(fragmentListDetailsToolbar) {
          title = details.name
          subtitle = details.description
        }
        val isQuickRemoveEnabled = isQuickRemoveEnabled
        fragmentListDetailsSortButton.onClick { showSortOrderDialog(details.sortByLocal, details.filterTypeLocal) }
        fragmentListDetailsMoreButton.onClick { openPopupMenu(isQuickRemoveEnabled) }
      }
      listItems?.let {
        val isRealEmpty = it.isEmpty() && listDetails?.filterTypeLocal?.containsAll(Mode.getAll()) == true
        fragmentListDetailsEmptyView.fadeIf(it.isEmpty())
        fragmentListDetailsManageButton.visibleIf(!isRealEmpty)
        fragmentListDetailsSortButton.visibleIf(!isRealEmpty)
        val scrollTop = resetScroll?.consume() == true
        adapter?.setItems(it, scrollTop)
      }
      isManageMode.let { isEnabled ->
        if (listItems?.isEmpty() == true && listDetails?.filterTypeLocal?.containsAll(Mode.getAll()) == true) {
          return@let
        }

        fragmentListDetailsSortButton.visibleIf(!isEnabled)
        fragmentListDetailsManageButton.visibleIf(!isEnabled)
        fragmentListDetailsMoreButton.visibleIf(!isEnabled)

        if (isEnabled) {
          fragmentListDetailsToolbar.title = getString(R.string.textChangeRanks)
          fragmentListDetailsToolbar.subtitle = getString(R.string.textChangeRanksSubtitle)
        } else {
          fragmentListDetailsToolbar.title = listDetails?.name ?: list.name
          fragmentListDetailsToolbar.subtitle = listDetails?.description
        }
      }
      isLoading.let {
        fragmentListDetailsLoadingView.visibleIf(it)
        if (it) disableUi() else enableUi()
      }
      deleteEvent?.let { event -> event.consume()?.let { activity?.onBackPressed() } }
    }
  }

  override fun onListItemDragStarted(viewHolder: RecyclerView.ViewHolder) {
    touchHelper?.startDrag(viewHolder)
  }

  override fun onListItemSwipeStarted(viewHolder: RecyclerView.ViewHolder) {
    touchHelper?.startSwipe(viewHolder)
  }

  override fun onDestroyView() {
    adapter = null
    touchHelper = null
    layoutManager = null
    super.onDestroyView()
  }
}
