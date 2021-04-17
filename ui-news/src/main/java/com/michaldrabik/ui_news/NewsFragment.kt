package com.michaldrabik.ui_news

import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.events.Event
import com.michaldrabik.ui_base.events.EventObserver
import com.michaldrabik.ui_base.utilities.extensions.addDivider
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.enableUi
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.updateTopMargin
import com.michaldrabik.ui_news.di.UiNewsComponentProvider
import com.michaldrabik.ui_news.recycler.NewsAdapter
import kotlinx.android.synthetic.main.fragment_news.*

class NewsFragment :
  BaseFragment<NewsViewModel>(R.layout.fragment_news),
  OnTabReselectedListener,
  EventObserver {

  override val viewModel by viewModels<NewsViewModel> { viewModelFactory }

  private var adapter: NewsAdapter? = null
  private var layoutManager: LinearLayoutManager? = null

  private var headerTranslation = 0F
  private var tabsTranslation = 0F

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiNewsComponentProvider).provideNewsComponent().inject(this)
    super.onCreate(savedInstanceState)
    setupBackPressed()

    savedInstanceState?.let {
      headerTranslation = it.getFloat("ARG_HEADER_POSITION")
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
    }
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putFloat("ARG_HEADER_POSITION", fragmentNewsHeaderView?.translationY ?: 0F)
//    outState.putFloat("ARG_TABS_POSITION", fragmentListsModeTabs?.translationY ?: 0F)
//    outState.putBoolean("ARG_FAB_HIDDEN", fragmentListsCreateListButton?.visibility != VISIBLE)
  }

  override fun onPause() {
    enableUi()
//    tabsTranslation = fragmentListsModeTabs.translationY
    headerTranslation = fragmentNewsHeaderView.translationY
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

    fragmentNewsHeaderView.translationY = headerTranslation
//    fragmentListsModeTabs.translationY = tabsTranslation
//    fragmentListsSortButton.translationY = tabsTranslation
  }

  private fun setupStatusBar() {
    fragmentNewsRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      fragmentNewsRecycler.updatePadding(top = statusBarSize + dimenToPx(R.dimen.newsRecyclerTopPadding))
      fragmentNewsHeaderView.updateTopMargin(dimenToPx(R.dimen.spaceSmall) + statusBarSize)
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = NewsAdapter().apply {
      stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }
    fragmentNewsRecycler.apply {
      adapter = this@NewsFragment.adapter
      layoutManager = this@NewsFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
      addDivider(R.drawable.divider_news, VERTICAL)
    }
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
      items?.let {
        fragmentNewsRecycler.fadeIf(it.isNotEmpty())
        fragmentNewsEmptyView.fadeIf(it.isEmpty())
        adapter?.setItems(it)
      }
    }
  }

  private fun openSettings() {
    hideNavigation()
    navigateTo(R.id.actionListsFragmentToSettingsFragment)
  }

  private fun scrollToTop(smooth: Boolean = true) {
//    fragmentListsModeTabs.animate().translationY(0F).start()
    fragmentNewsHeaderView.animate().translationY(0F).start()
//    fragmentListsSortButton.animate().translationY(0F).start()
    when {
      smooth -> fragmentNewsRecycler.smoothScrollToPosition(0)
      else -> fragmentNewsRecycler.scrollToPosition(0)
    }
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
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
