package com.michaldrabik.ui_lists.lists

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.common.views.exSearchViewIcon
import com.michaldrabik.ui_base.common.views.exSearchViewInput
import com.michaldrabik.ui_base.common.views.exSearchViewText
import com.michaldrabik.ui_base.events.Event
import com.michaldrabik.ui_base.events.EventObserver
import com.michaldrabik.ui_base.events.TraktListQuickSyncSuccess
import com.michaldrabik.ui_base.events.TraktQuickSyncSuccess
import com.michaldrabik.ui_base.utilities.NavigationHost
import com.michaldrabik.ui_base.utilities.extensions.add
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.disableUi
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.enableUi
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.hideKeyboard
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showKeyboard
import com.michaldrabik.ui_base.utilities.extensions.updateTopMargin
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.lists.recycler.ListsAdapter
import com.michaldrabik.ui_lists.lists.recycler.ListsItem
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_UPDATED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_LIST
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_CREATE_LIST
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_lists.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListsFragment :
  BaseFragment<ListsViewModel>(R.layout.fragment_lists),
  OnTraktSyncListener,
  OnTabReselectedListener,
  EventObserver {

  override val viewModel by viewModels<ListsViewModel>()

  private var adapter: ListsAdapter? = null
  private var layoutManager: LinearLayoutManager? = null

  private var searchViewTranslation = 0F
  private var tabsTranslation = 0F
  private var isFabHidden = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    savedInstanceState?.let {
      searchViewTranslation = it.getFloat("ARG_SEARCH_POSITION")
      tabsTranslation = it.getFloat("ARG_TABS_POSITION")
      isFabHidden = it.getBoolean("ARG_FAB_HIDDEN")
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupStatusBar()
    setupRecycler()

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          loadItems(resetScroll = false)
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putFloat("ARG_SEARCH_POSITION", fragmentListsSearchView?.translationY ?: 0F)
    outState.putFloat("ARG_TABS_POSITION", fragmentListsModeTabs?.translationY ?: 0F)
    outState.putBoolean("ARG_FAB_HIDDEN", fragmentListsCreateListButton?.visibility != VISIBLE)
  }

  override fun onPause() {
    enableUi()
    tabsTranslation = fragmentListsModeTabs.translationY
    searchViewTranslation = fragmentListsSearchView.translationY
    super.onPause()
  }

  private fun setupView() {
    fragmentListsSearchView.run {
      hint = getString(R.string.textSearchFor)
      onSettingsClickListener = { openSettings() }
      if (isTraktSyncing()) setTraktProgress(true)
    }
    fragmentListsModeTabs.run {
      onModeSelected = { (requireActivity() as NavigationHost).setMode(it, force = true) }
      showMovies(moviesEnabled)
      showLists(true, anchorEnd = moviesEnabled)
      selectLists()
    }
    fragmentListsCreateListButton.run {
      if (!isFabHidden) fadeIn()
      onClick { openCreateList() }
    }
    fragmentListsSortButton.onClick {
      viewModel.loadSortOrder()
    }
    fragmentListsSearchView.onClick { enterSearch() }
    exSearchViewInput.run {
      imeOptions = EditorInfo.IME_ACTION_DONE
      setOnEditorActionListener { _, _, _ ->
        clearFocus()
        hideKeyboard()
        true
      }
    }

    fragmentListsSearchView.translationY = searchViewTranslation
    fragmentListsModeTabs.translationY = tabsTranslation
    fragmentListsSortButton.translationY = tabsTranslation
  }

  private fun setupStatusBar() {
    fragmentListsRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      fragmentListsSearchView.applyWindowInsetBehaviour(dimenToPx(R.dimen.spaceNormal) + statusBarSize)
      fragmentListsSearchView.updateTopMargin(dimenToPx(R.dimen.spaceSmall) + statusBarSize)
      fragmentListsModeTabs.updateTopMargin(dimenToPx(R.dimen.collectionTabsMargin) + statusBarSize)
      fragmentListsSortButton.updateTopMargin(dimenToPx(R.dimen.listsSortIconPadding) + statusBarSize)
      fragmentListsEmptyView.updateTopMargin(statusBarSize)
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = ListsAdapter().apply {
      stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
      itemClickListener = { openListDetails(it) }
      itemsChangedListener = { scrollToTop(smooth = false) }
      missingImageListener = { item, itemImage, force ->
        viewModel.loadMissingImage(item, itemImage, force)
      }
    }
    fragmentListsRecycler.apply {
      adapter = this@ListsFragment.adapter
      layoutManager = this@ListsFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
      clearOnScrollListeners()
      addOnScrollListener(object : RecyclerView.OnScrollListener() {
        var isFading = false
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
          if (isFading) return
          val position = this@ListsFragment.layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
          if (position > 1) {
            if (fragmentListsCreateListButton.visibility != VISIBLE) return
            fragmentListsCreateListButton
              .fadeOut(125, endAction = { isFading = false })
              .add(animations)
          } else {
            if (fragmentListsCreateListButton.visibility != GONE) return
            fragmentListsCreateListButton
              .fadeIn(125, endAction = { isFading = false })
              .add(animations)
          }
          isFading = true
        }
      })
    }
  }

  override fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      if (fragmentListsSearchView.isSearching) {
        exitSearch()
      } else {
        isEnabled = false
        activity?.onBackPressed()
      }
    }
  }

  private fun enterSearch() {
    if (fragmentListsSearchView.isSearching) return
    fragmentListsSearchView.isSearching = true
    exSearchViewText.gone()
    exSearchViewInput.run {
      setText("")
      doAfterTextChanged {
        viewModel.loadItems(searchQuery = it.toString().trim(), resetScroll = true)
      }
      visible()
      showKeyboard()
      requestFocus()
    }
    (exSearchViewIcon.drawable as Animatable).start()
    exSearchViewIcon.onClick { exitSearch() }
  }

  private fun exitSearch() {
    fragmentListsSearchView.isSearching = false
    exSearchViewText.visible()
    exSearchViewInput.run {
      setText("")
      gone()
      hideKeyboard()
      clearFocus()
    }
    exSearchViewIcon.setImageResource(R.drawable.ic_anim_search_to_close)
  }

  private fun showSortOrderDialog(order: SortOrder) {
    val options = listOf(NAME, NEWEST, DATE_UPDATED)
    val optionsStrings = options.map { getString(it.displayString) }.toTypedArray()

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setTitle(R.string.textSortBy)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(optionsStrings, options.indexOf(order)) { dialog, index ->
        viewModel.setSortOrder(options[index])
        dialog.dismiss()
      }
      .show()
  }

  private fun render(uiState: ListsUiState) {
    uiState.run {
      items?.let {
        val isSearching = fragmentListsSearchView.isSearching
        fragmentListsEmptyView.fadeIf(it.isEmpty() && !isSearching)
        fragmentListsSortButton.visibleIf(it.isNotEmpty())
        fragmentListsSortButton.isEnabled = it.isNotEmpty() && !isSearching

        if (!isSearching) {
          fragmentListsSearchView.isClickable = it.isNotEmpty()
          fragmentListsSearchView.isEnabled = it.isNotEmpty()
        }

        val resetScroll = resetScroll.consume() == true
        adapter?.setItems(it, resetScroll)
      }
      sortOrder?.let { event ->
        event.consume()?.let { showSortOrderDialog(it) }
      }
    }
  }

  private fun openListDetails(listItem: ListsItem) {
    disableUi()
    hideNavigation()
    fragmentListsRoot.fadeOut(150) {
      exitSearch()
      val bundle = bundleOf(ARG_LIST to listItem.list)
      navigateTo(R.id.actionListsFragmentToDetailsFragment, bundle)
    }.add(animations)
  }

  private fun openSettings() {
    hideNavigation()
    navigateTo(R.id.actionListsFragmentToSettingsFragment)
  }

  private fun openCreateList() {
    setFragmentResultListener(REQUEST_CREATE_LIST) { _, _ -> viewModel.loadItems(resetScroll = true) }
    navigateTo(R.id.actionListsFragmentToCreateListDialog, bundleOf())
  }

  private fun scrollToTop(smooth: Boolean = true) {
    fragmentListsModeTabs.animate().translationY(0F).start()
    fragmentListsSearchView.animate().translationY(0F).start()
    fragmentListsSortButton.animate().translationY(0F).start()
    when {
      smooth -> fragmentListsRecycler.smoothScrollToPosition(0)
      else -> fragmentListsRecycler.scrollToPosition(0)
    }
  }

  override fun onTraktSyncProgress() =
    fragmentListsSearchView.setTraktProgress(true)

  override fun onTraktSyncComplete() {
    fragmentListsSearchView.setTraktProgress(false)
    viewModel.loadItems(resetScroll = true)
  }

  override fun onNewEvent(event: Event) {
    activity?.runOnUiThread {
      when (event) {
        is TraktListQuickSyncSuccess -> {
          val text = resources.getQuantityString(R.plurals.textTraktQuickSyncComplete, 1, 1)
          fragmentListsSnackHost.showInfoSnackbar(text)
        }
        is TraktQuickSyncSuccess -> {
          val text = resources.getQuantityString(R.plurals.textTraktQuickSyncComplete, event.count, event.count)
          fragmentListsSnackHost.showInfoSnackbar(text)
        }
        else -> Unit
      }
    }
  }

  override fun onTabReselected() = scrollToTop()

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
