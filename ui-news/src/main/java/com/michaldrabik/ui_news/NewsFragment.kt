package com.michaldrabik.ui_news

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.events.Event
import com.michaldrabik.ui_base.events.EventObserver
import com.michaldrabik.ui_base.utilities.extensions.enableUi
import com.michaldrabik.ui_news.di.UiNewsComponentProvider

class NewsFragment :
  BaseFragment<NewsViewModel>(R.layout.fragment_news),
  OnTraktSyncListener,
  OnTabReselectedListener,
  EventObserver {

  override val viewModel by viewModels<NewsViewModel> { viewModelFactory }

  //  private var adapter: ListsAdapter? = null
  private var layoutManager: LinearLayoutManager? = null

  private var searchViewTranslation = 0F
  private var tabsTranslation = 0F
  private var isFabHidden = false

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiNewsComponentProvider).provideNewsComponent().inject(this)
    super.onCreate(savedInstanceState)
    setupBackPressed()

    savedInstanceState?.let {
//      searchViewTranslation = it.getFloat("ARG_SEARCH_POSITION")
//      tabsTranslation = it.getFloat("ARG_TABS_POSITION")
//      isFabHidden = it.getBoolean("ARG_FAB_HIDDEN")
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupStatusBar()
    setupRecycler()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it) })
//      loadItems(resetScroll = false)
    }
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
//    outState.putFloat("ARG_SEARCH_POSITION", fragmentListsSearchView?.translationY ?: 0F)
//    outState.putFloat("ARG_TABS_POSITION", fragmentListsModeTabs?.translationY ?: 0F)
//    outState.putBoolean("ARG_FAB_HIDDEN", fragmentListsCreateListButton?.visibility != VISIBLE)
  }

  override fun onPause() {
    enableUi()
//    tabsTranslation = fragmentListsModeTabs.translationY
//    searchViewTranslation = fragmentListsSearchView.translationY
    super.onPause()
  }

  private fun setupView() {
//    fragmentListsSearchView.run {
//      hint = getString(R.string.textSearchFor)
//      onSettingsClickListener = { openSettings() }
//      if (isTraktSyncing()) setTraktProgress(true)
//    }
//    fragmentListsModeTabs.run {
//      onModeSelected = { (requireActivity() as NavigationHost).setMode(it, force = true) }
//      showMovies(moviesEnabled)
//      showLists(true, anchorEnd = moviesEnabled)
//      selectLists()
//    }
//    fragmentListsCreateListButton.run {
//      if (!isFabHidden) fadeIn()
//      onClick { openCreateList() }
//    }
//    fragmentListsSortButton.onClick {
//      viewModel.loadSortOrder()
//    }
//    fragmentListsSearchView.onClick { enterSearch() }
//    exSearchViewInput.run {
//      imeOptions = EditorInfo.IME_ACTION_DONE
//      setOnEditorActionListener { _, _, _ ->
//        clearFocus()
//        hideKeyboard()
//        true
//      }
//    }

//    fragmentListsSearchView.translationY = searchViewTranslation
//    fragmentListsModeTabs.translationY = tabsTranslation
//    fragmentListsSortButton.translationY = tabsTranslation
  }

  private fun setupStatusBar() {
//    fragmentListsRoot.doOnApplyWindowInsets { _, insets, _, _ ->
//      val statusBarSize = insets.systemWindowInsetTop
//      fragmentListsSearchView.applyWindowInsetBehaviour(dimenToPx(R.dimen.spaceNormal) + statusBarSize)
//      fragmentListsSearchView.updateTopMargin(dimenToPx(R.dimen.spaceSmall) + statusBarSize)
//      fragmentListsModeTabs.updateTopMargin(dimenToPx(R.dimen.collectionTabsMargin) + statusBarSize)
//      fragmentListsSortButton.updateTopMargin(dimenToPx(R.dimen.listsSortIconPadding) + statusBarSize)
//      fragmentListsEmptyView.updateTopMargin(statusBarSize)
//    }
  }

  private fun setupRecycler() {
//    layoutManager = LinearLayoutManager(context, VERTICAL, false)
//    adapter = ListsAdapter().apply {
//      stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
//      itemClickListener = { openListDetails(it) }
//      itemsChangedListener = { scrollToTop(smooth = false) }
//      missingImageListener = { item, itemImage, force ->
//        viewModel.loadMissingImage(item, itemImage, force)
//      }
//    }
//    fragmentListsRecycler.apply {
//      adapter = this@NewsFragment.adapter
//      layoutManager = this@NewsFragment.layoutManager
//      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
//      setHasFixedSize(true)
//    }
  }

  private fun setupBackPressed() {
//    val dispatcher = requireActivity().onBackPressedDispatcher
//    dispatcher.addCallback(this) {
//      if (fragmentListsSearchView.isSearching) {
//        exitSearch()
//      } else {
//        isEnabled = false
//        dispatcher.onBackPressed()
//      }
//    }
  }

  private fun render(uiModel: NewsUiModel) {
    uiModel.run {
//      items?.let {
//        val isSearching = fragmentListsSearchView.isSearching
//        fragmentListsEmptyView.fadeIf(it.isEmpty() && !isSearching)
//        fragmentListsSortButton.visibleIf(it.isNotEmpty())
//        fragmentListsSortButton.isEnabled = it.isNotEmpty() && !isSearching
//
//        if (!isSearching) {
//          fragmentListsSearchView.isClickable = it.isNotEmpty()
//          fragmentListsSearchView.isEnabled = it.isNotEmpty()
//        }
//
//        val resetScroll = resetScroll?.consume() == true
//        adapter?.setItems(it, resetScroll)
//      }
//      sortOrderEvent?.let { event ->
//        event.consume()?.let { showSortOrderDialog(it) }
//      }
    }
  }

  private fun openSettings() {
    hideNavigation()
    navigateTo(R.id.actionListsFragmentToSettingsFragment)
  }

  private fun scrollToTop(smooth: Boolean = true) {
//    fragmentListsModeTabs.animate().translationY(0F).start()
//    fragmentListsSearchView.animate().translationY(0F).start()
//    fragmentListsSortButton.animate().translationY(0F).start()
//    when {
//      smooth -> fragmentListsRecycler.smoothScrollToPosition(0)
//      else -> fragmentListsRecycler.scrollToPosition(0)
//    }
  }

  override fun onTraktSyncProgress() {
//    fragmentListsSearchView.setTraktProgress(true)
  }

  override fun onTraktSyncComplete() {
//    fragmentListsSearchView.setTraktProgress(false)
//    viewModel.loadItems(resetScroll = true)
  }

  override fun onNewEvent(event: Event) {
    activity?.runOnUiThread {
//      when (event) {
//        is TraktListQuickSyncSuccess -> {
//          val text = resources.getQuantityString(R.plurals.textTraktQuickSyncComplete, 1, 1)
//          fragmentListsSnackHost.showInfoSnackbar(text)
//        }
//        is TraktQuickSyncSuccess -> {
//          val text = resources.getQuantityString(R.plurals.textTraktQuickSyncComplete, event.count, event.count)
//          fragmentListsSnackHost.showInfoSnackbar(text)
//        }
//        else -> Unit
//      }
    }
  }

  override fun onTabReselected() = scrollToTop()

  override fun onDestroyView() {
//    adapter = null
//    layoutManager = null
    super.onDestroyView()
  }
}
