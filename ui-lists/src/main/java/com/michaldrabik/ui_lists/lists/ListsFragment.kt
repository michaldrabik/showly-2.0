package com.michaldrabik.ui_lists.lists

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.common.views.exSearchViewInput
import com.michaldrabik.ui_base.utilities.NavigationHost
import com.michaldrabik.ui_base.utilities.extensions.add
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.hideKeyboard
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.updateTopMargin
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.lists.di.UiListsComponentProvider
import com.michaldrabik.ui_lists.lists.recycler.ListsAdapter
import com.michaldrabik.ui_lists.lists.recycler.ListsItem
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_UPDATED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_LIST
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_CREATE_LIST
import kotlinx.android.synthetic.main.fragment_lists.*

class ListsFragment :
  BaseFragment<ListsViewModel>(R.layout.fragment_lists),
  OnTraktSyncListener {

  override val viewModel by viewModels<ListsViewModel> { viewModelFactory }

  private var adapter: ListsAdapter? = null
  private var layoutManager: LinearLayoutManager? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiListsComponentProvider).provideListsComponent().inject(this)
    super.onCreate(savedInstanceState)
    setupBackPressed()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupStatusBar()
    setupRecycler()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it) })
      loadItems(resetScroll = false)
    }
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
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
      fadeIn()
      onClick { openCreateList() }
    }
    fragmentListsSortButton.onClick {
      viewModel.loadSortOrder()
    }
    exSearchViewInput.run {
      imeOptions = EditorInfo.IME_ACTION_DONE
      setOnEditorActionListener { _, _, _ ->
        clearFocus()
        hideKeyboard()
        true
      }
    }

    fragmentListsSearchView.translationY = viewModel.searchViewTranslation
    fragmentListsModeTabs.translationY = viewModel.tabsTranslation
    fragmentListsSortButton.translationY = viewModel.tabsTranslation
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
      itemClickListener = { openListDetails(it) }
      itemsChangedListener = {
        fragmentListsRecycler.scrollToPosition(0)
        fragmentListsModeTabs.animate().translationY(0F).start()
        fragmentListsSearchView.animate().translationY(0F).start()
        fragmentListsSortButton.animate().translationY(0F).start()
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

  private fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(this) {
      if (fragmentListsSearchView.isSearching) {
//        exitSearch()
      } else {
        isEnabled = false
        dispatcher.onBackPressed()
      }
    }
  }

//  private fun enterSearch() {
//    if (followedMoviesSearchView.isSearching) return
//    followedMoviesSearchView.isSearching = true
//    exSearchViewText.gone()
//    exSearchViewInput.run {
//      setText("")
//      doAfterTextChanged { viewModel.searchMovies(it?.toString() ?: "") }
//      visible()
//      showKeyboard()
//      requestFocus()
//    }
//    (exSearchViewIcon.drawable as Animatable).start()
//    exSearchViewIcon.onClick { exitSearch() }
//    hideNavigation(false)
//  }
//
//  private fun exitSearch(showNavigation: Boolean = true) {
//    followedMoviesSearchView.isSearching = false
//    exSearchViewText.visible()
//    exSearchViewInput.run {
//      setText("")
//      gone()
//      hideKeyboard()
//      clearFocus()
//    }
//    exSearchViewIcon.setImageResource(R.drawable.ic_anim_search_to_close)
//    if (showNavigation) showNavigation()
//  }

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

  private fun render(uiModel: ListsUiModel) {
    uiModel.run {
      items?.let {
        fragmentListsEmptyView.fadeIf(it.isEmpty())
        fragmentListsSortButton.visibleIf(it.isNotEmpty())
        val resetScroll = resetScroll?.consume() == true
        adapter?.setItems(it, resetScroll)
      }
      sortOrderEvent?.let { event ->
        event.consume()?.let { showSortOrderDialog(it) }
      }
    }
  }

  private fun openListDetails(listItem: ListsItem) {
    val bundle = bundleOf(ARG_LIST to listItem.list)
    navigateTo(R.id.actionListsFragmentToDetailsFragment, bundle)

    viewModel.tabsTranslation = fragmentListsModeTabs.translationY
    viewModel.searchViewTranslation = fragmentListsSearchView.translationY
  }

  private fun openSettings() {
    hideNavigation()
    navigateTo(R.id.actionListsFragmentToSettingsFragment)

    viewModel.tabsTranslation = fragmentListsModeTabs.translationY
    viewModel.searchViewTranslation = fragmentListsSearchView.translationY
  }

  private fun openCreateList() {
    setFragmentResultListener(REQUEST_CREATE_LIST) { _, _ -> viewModel.loadItems(resetScroll = true) }
    navigateTo(R.id.actionListsFragmentToCreateListDialog, bundleOf())
  }

  fun enableSearch(enable: Boolean) {
    fragmentListsSearchView.isClickable = enable
    fragmentListsSearchView.isEnabled = enable
  }

  override fun onTraktSyncProgress() =
    fragmentListsSearchView.setTraktProgress(true)

  override fun onTraktSyncComplete() {
    fragmentListsSearchView.setTraktProgress(false)
  }

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
