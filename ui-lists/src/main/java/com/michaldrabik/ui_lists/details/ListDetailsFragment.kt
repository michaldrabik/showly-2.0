package com.michaldrabik.ui_lists.details

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.sheets.sort_order.SortOrderBottomSheet
import com.michaldrabik.ui_base.utilities.extensions.add
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.disableUi
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.enableUi
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.requireParcelable
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.details.helpers.ListItemDragListener
import com.michaldrabik.ui_lists.details.helpers.ListItemSwipeListener
import com.michaldrabik.ui_lists.details.helpers.ReorderListCallback
import com.michaldrabik.ui_lists.details.helpers.ReorderListCallbackAdapter
import com.michaldrabik.ui_lists.details.recycler.ListDetailsAdapter
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import com.michaldrabik.ui_lists.details.views.ListDetailsDeleteConfirmView
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RANK
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_LIST
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SELECTED_SORT_ORDER
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SELECTED_SORT_TYPE
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_SORT_ORDER
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_list_details.*
import kotlinx.android.synthetic.main.view_list_delete_confirm.view.*

@AndroidEntryPoint
class ListDetailsFragment :
  BaseFragment<ListDetailsViewModel>(R.layout.fragment_list_details), ListItemDragListener, ListItemSwipeListener {

  companion object {
    private const val ARG_HEADER_TRANSLATION = "ARG_HEADER_TRANSLATION"
  }

  override val viewModel by viewModels<ListDetailsViewModel>()

  private val list by lazy { requireParcelable<CustomList>(ARG_LIST) }

  private val recyclerPaddingBottom by lazy { requireContext().dimenToPx(R.dimen.spaceSmall) }
  private val recyclerPaddingTop by lazy { requireContext().dimenToPx(R.dimen.listDetailsRecyclerTopPadding) }

  private var adapter: ListDetailsAdapter? = null
  private var touchHelper: ItemTouchHelper? = null
  private var layoutManager: LinearLayoutManager? = null

  private var headerTranslation = 0F
  private var isReorderMode = false

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    savedInstanceState?.let {
      headerTranslation = it.getFloat(ARG_HEADER_TRANSLATION)
    }
    return super.onCreateView(inflater, container, savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.messageFlow.collect { showSnack(it) } },
      doAfterLaunch = { viewModel.loadDetails(list.id) }
    )
  }

  override fun onResume() {
    super.onResume()
    hideNavigation()
  }

  override fun onPause() {
    enableUi()
    headerTranslation = fragmentListDetailsChipsView.translationY
    super.onPause()
  }

  private fun setupView() {
    fragmentListDetailsRoot.doOnApplyWindowInsets { view, insets, padding, _ ->
      val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      view.updatePadding(top = padding.top + inset)
    }
    with(fragmentListDetailsToolbar) {
      title = list.name
      subtitle = list.description
      setNavigationOnClickListener {
        if (isReorderMode) toggleReorderMode()
        else activity?.onBackPressed()
      }
    }
    with(fragmentListDetailsChipsView) {
      onChipsChangeListener = { viewModel.setFilterTypes(list.id, it) }
      translationY = headerTranslation
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
        fragmentListDetailsChipsView.translationY = 0F
      },
      itemsClearedListener = {
        if (isReorderMode) viewModel.updateRanks(list.id, it)
      },
      itemsSwipedListener = {
        viewModel.deleteListItem(list.id, it)
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

  private fun showSortOrderDialog(order: SortOrder, type: SortType) {
    val options = listOf(RANK, NAME, NEWEST, RATING, DATE_ADDED)
    val args = SortOrderBottomSheet.createBundle(options, order, type)

    setFragmentResultListener(REQUEST_SORT_ORDER) { _, bundle ->
      val sortOrder = bundle.getSerializable(ARG_SELECTED_SORT_ORDER) as SortOrder
      val sortType = bundle.getSerializable(ARG_SELECTED_SORT_TYPE) as SortType
      viewModel.setSortOrder(list.id, sortOrder, sortType)
    }

    navigateTo(R.id.actionListDetailsFragmentToSortOrder, args)
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

    fun renderTitle(name: String?, itemsCount: Int? = null) {
      if (name.isNullOrBlank()) return
      fragmentListDetailsToolbar.title = when {
        itemsCount != null && itemsCount > 0 -> "$name ($itemsCount)"
        else -> name
      }
    }

    uiState.run {
      renderTitle(listDetails?.name, listItems?.size)
      listDetails?.let { details ->
        val isQuickRemoveEnabled = isQuickRemoveEnabled
        fragmentListDetailsToolbar.subtitle = details.description
        fragmentListDetailsSortButton.onClick { showSortOrderDialog(details.sortByLocal, details.sortHowLocal) }
        fragmentListDetailsMoreButton.onClick { openPopupMenu(isQuickRemoveEnabled) }
        fragmentListDetailsChipsView.setTypes(details.filterTypeLocal)
      }
      listItems?.let {
        val isRealEmpty = it.isEmpty() && listDetails?.filterTypeLocal?.containsAll(Mode.getAll()) == true
        fragmentListDetailsEmptyView.fadeIf(it.isEmpty())
        fragmentListDetailsManageButton.visibleIf(!isRealEmpty)
        fragmentListDetailsSortButton.visibleIf(!isRealEmpty)

        val scrollTop = resetScroll?.consume() == true
        adapter?.setItems(it, scrollTop)
      }
      isManageMode.let { isManageMode ->
        if (listItems?.isEmpty() == true && listDetails?.filterTypeLocal?.containsAll(Mode.getAll()) == true) {
          return@let
        }

        fragmentListDetailsSortButton.visibleIf(!isManageMode)
        fragmentListDetailsManageButton.visibleIf(!isManageMode)
        fragmentListDetailsMoreButton.visibleIf(!isManageMode)

        if (isManageMode) {
          fragmentListDetailsToolbar.title = getString(R.string.textChangeRanks)
          fragmentListDetailsToolbar.subtitle = getString(R.string.textChangeRanksSubtitle)
          fragmentListDetailsRecycler.setPadding(0, 0, 0, recyclerPaddingBottom)
        } else {
          renderTitle(listDetails?.name ?: list.name, listItems?.size)
          fragmentListDetailsToolbar.subtitle = listDetails?.description
          fragmentListDetailsRecycler.setPadding(0, recyclerPaddingTop, 0, recyclerPaddingBottom)
        }

        if (resetScroll?.consume() == true) {
          fragmentListDetailsRecycler.scrollToPosition(0)
          fragmentListDetailsChipsView.translationY = 0F
        }
      }
      isFiltersVisible.let {
        fragmentListDetailsChipsView.visibleIf(it)
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

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putFloat(ARG_HEADER_TRANSLATION, fragmentListDetailsChipsView?.translationY ?: 0F)
  }

  override fun onDestroyView() {
    adapter = null
    touchHelper = null
    layoutManager = null
    super.onDestroyView()
  }
}
