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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import com.michaldrabik.ui_lists.details.di.UiListDetailsComponentProvider
import com.michaldrabik.ui_lists.details.helpers.ListItemDragListener
import com.michaldrabik.ui_lists.details.helpers.ListItemSwipeListener
import com.michaldrabik.ui_lists.details.helpers.ReorderListCallback
import com.michaldrabik.ui_lists.details.helpers.ReorderListCallbackAdapter
import com.michaldrabik.ui_lists.details.recycler.ListDetailsAdapter
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_model.SortOrderList
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_LIST
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import kotlinx.android.synthetic.main.fragment_list_details.*
import kotlinx.android.synthetic.main.fragment_lists.*

class ListDetailsFragment :
  BaseFragment<ListDetailsViewModel>(R.layout.fragment_list_details), ListItemDragListener, ListItemSwipeListener {

  override val viewModel by viewModels<ListDetailsViewModel> { viewModelFactory }

  private val list by lazy { requireArguments().getParcelable<CustomList>(ARG_LIST)!! }

  private var adapter: ListDetailsAdapter? = null
  private var touchHelper: ItemTouchHelper? = null
  private var layoutManager: LinearLayoutManager? = null

  private var isManageMode = false

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiListDetailsComponentProvider).provideListDetailsComponent().inject(this)
    super.onCreate(savedInstanceState)
    setupBackPressed()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it) })
      messageLiveData.observe(viewLifecycleOwner, { showSnack(it) })
      loadDetails(list.id)
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
      if (!list.description.isNullOrBlank()) {
        subtitle = list.description
      }
      setNavigationOnClickListener {
        if (isManageMode) toggleManageMode()
        else activity?.onBackPressed()
      }
    }
    fragmentListDetailsMoreButton.onClick { openPopupMenu() }
    fragmentListDetailsManageButton.onClick { toggleManageMode() }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = ListDetailsAdapter(
      itemClickListener = { openItemDetails(it) },
      missingImageListener = { item: ListDetailsItem, force: Boolean -> viewModel.loadMissingImage(item, force) },
      missingTranslationListener = { viewModel.loadMissingTranslation(it) },
      itemsChangedListener = { fragmentListDetailsRecycler.scrollToPosition(0) },
      itemsMovedListener = { viewModel.updateRanks(list.id, it) },
      itemsSwipedListener = { viewModel.deleteListItem(list.id, it) },
      itemDragStartListener = this,
      itemSwipeStartListener = this
    )
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

  private fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(this) {
      if (isManageMode) {
        toggleManageMode()
      } else {
        remove()
        findNavControl()?.popBackStack()
      }
    }
  }

  private fun showSortOrderDialog(order: SortOrderList) {
    val options = SortOrderList.values()
    val optionsStrings = options.map { getString(it.displayString) }.toTypedArray()

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setTitle(R.string.textSortBy)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(optionsStrings, options.indexOf(order)) { dialog, index ->
        viewModel.setSortOrder(list.id, options[index])
        dialog.dismiss()
      }
      .show()
  }

  private fun showDeleteDialog() {
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setTitle(R.string.textConfirmDeleteListTitle)
      .setMessage(R.string.textConfirmDeleteListSubtitle)
      .setPositiveButton(R.string.textYes) { _, _ -> viewModel.deleteList(list.id) }
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

  private fun openPopupMenu() {
    PopupMenu(requireContext(), fragmentListDetailsMoreButton, Gravity.CENTER).apply {
      inflate(R.menu.menu_list_details)
      setOnMenuItemClickListener { menuItem ->
        when (menuItem.itemId) {
          R.id.menuListDetailsEdit -> showEditDialog()
          R.id.menuListDetailsDelete -> showDeleteDialog()
        }
        true
      }
      show()
    }
  }

  private fun toggleManageMode() {
    isManageMode = !isManageMode
    viewModel.setManageMode(list.id, isManageMode)
  }

  private fun render(uiModel: ListDetailsUiModel) {
    uiModel.run {
      details?.let { details ->
        with(fragmentListDetailsToolbar) {
          title = details.name
          if (!details.description.isNullOrBlank()) {
            subtitle = details.description
          }
        }
        fragmentListDetailsSortButton.onClick { showSortOrderDialog(details.sortByLocal) }
      }
      items?.let {
        fragmentListDetailsEmptyView.fadeIf(it.isEmpty())
        fragmentListDetailsManageButton.visibleIf(it.isNotEmpty())
        fragmentListDetailsSortButton.visibleIf(it.isNotEmpty())
        val scrollTop = resetScroll?.consume() == true
        adapter?.setItems(it, scrollTop)
      }
      isManageMode?.let { isEnabled ->
        if (items?.isEmpty() == true) return@let

        fragmentListDetailsSortButton.visibleIf(!isEnabled)
        fragmentListDetailsManageButton.visibleIf(!isEnabled)
        fragmentListDetailsMoreButton.visibleIf(!isEnabled)

        if (isEnabled) {
          fragmentListDetailsToolbar.title = getString(R.string.textChangeRanks)
          fragmentListDetailsToolbar.subtitle = getString(R.string.textChangeRanksSubtitle)
        } else {
          fragmentListDetailsToolbar.title = details?.name ?: list.name
          if (!list.description.isNullOrBlank()) {
            fragmentListDetailsToolbar.subtitle = details?.description ?: list.description
          } else {
            fragmentListDetailsToolbar.subtitle = null
          }
        }
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
