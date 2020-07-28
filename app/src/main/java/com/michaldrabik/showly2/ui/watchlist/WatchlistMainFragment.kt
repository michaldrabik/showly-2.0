package com.michaldrabik.showly2.ui.watchlist

import android.annotation.SuppressLint
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateMargins
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.ui.common.OnEpisodesSyncedListener
import com.michaldrabik.showly2.ui.common.OnTraktSyncListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment
import com.michaldrabik.showly2.ui.watchlist.pages.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.showly2.utilities.extensions.fadeIf
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.hideKeyboard
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.showKeyboard
import com.michaldrabik.showly2.utilities.extensions.visible
import kotlinx.android.synthetic.main.fragment_watchlist_main.*
import kotlinx.android.synthetic.main.layout_watchlist_empty.*
import kotlinx.android.synthetic.main.view_search.*

class WatchlistMainFragment : BaseFragment<WatchlistMainViewModel>(R.layout.fragment_watchlist_main),
  OnEpisodesSyncedListener,
  OnTraktSyncListener {

  override val viewModel by viewModels<WatchlistMainViewModel> { viewModelFactory }

  private var searchViewTranslation = 0F
  private var tabsTranslation = 0F

  override fun onCreate(savedInstanceState: Bundle?) {
    fragmentComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupPager()
    setupStatusBar()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, Observer { render(it!!) })
      messageLiveData.observe(viewLifecycleOwner, Observer { showSnack(it) })
    }
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
    viewModel.loadWatchlist()
  }

  private fun setupView() {
    watchlistEmptyTraktButton.onClick { openTraktSync() }
    watchlistEmptyDiscoverButton.onClick { mainActivity().openTab(R.id.menuDiscover) }
    watchlistMainSearchView.run {
      hint = getString(R.string.textSearchWatchlist)
      settingsIconVisible = true
      isClickable = false
      onClick { enterSearch() }
      onSettingsClickListener = { openSettings() }
    }

    watchlistMainTabs.translationY = tabsTranslation
    watchlistMainSearchView.translationY = searchViewTranslation
  }

  @SuppressLint("WrongConstant")
  private fun setupPager() {
    watchlistMainPager.run {
      offscreenPageLimit = WatchlistMainPagesAdapter.PAGES_COUNT
      isUserInputEnabled = false
      adapter = WatchlistMainPagesAdapter(this@WatchlistMainFragment)
    }

    watchlistMainTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
      override fun onTabSelected(tab: TabLayout.Tab) {
        watchlistMainPager.currentItem = tab.position
      }

      override fun onTabReselected(tab: TabLayout.Tab) = Unit
      override fun onTabUnselected(tab: TabLayout.Tab) = Unit
    })
    TabLayoutMediator(watchlistMainTabs, watchlistMainPager) { tab, position ->
      tab.text = when (position) {
        0 -> getString(R.string.tabWatchlist)
        else -> getString(R.string.tabCalendar)
      }
    }.attach()
  }

  private fun setupStatusBar() {
    watchlistMainRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      (watchlistMainEmptyView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.spaceBig))
      (watchlistMainSearchView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.spaceSmall))
      (watchlistMainTabs.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.watchlistSearchViewPadding))
    }
  }

  fun openShowDetails(item: WatchlistItem) {
    exitSearch()
    hideNavigation()
    saveUiTranslations()
    watchlistMainRoot.fadeOut {
      val bundle = Bundle().apply { putLong(ShowDetailsFragment.ARG_SHOW_ID, item.show.ids.trakt.id) }
      navigateTo(R.id.actionWatchlistMainFragmentToShowDetailsFragment, bundle)
    }
  }

  private fun openSettings() {
    hideNavigation()
    navigateTo(R.id.actionWatchlistMainFragmentToSettingsFragment)
    saveUiTranslations()
  }

  private fun openTraktSync() {
    navigateTo(R.id.actionWatchlistMainFragmentToTraktSyncFragment)
    hideNavigation()
    saveUiTranslations()
  }

  private fun saveUiTranslations() {
    tabsTranslation = watchlistMainTabs.translationY
    searchViewTranslation = watchlistMainSearchView.translationY
  }

  private fun enterSearch() {
    if (watchlistMainSearchView.isSearching) return
    watchlistMainSearchView.isSearching = true
    searchViewText.gone()
    searchViewInput.run {
      setText("")
      doAfterTextChanged { viewModel.searchWatchlist(it?.toString() ?: "") }
      visible()
      showKeyboard()
      requestFocus()
    }
    (searchViewIcon.drawable as Animatable).start()
    searchViewIcon.onClick { exitSearch() }
    hideNavigation(false)
  }

  private fun exitSearch(showNavigation: Boolean = true) {
    watchlistMainSearchView.isSearching = false
    searchViewText.visible()
    searchViewInput.run {
      setText("")
      gone()
      hideKeyboard()
      clearFocus()
    }
    searchViewIcon.setImageResource(R.drawable.ic_anim_search_to_close)
    if (showNavigation) showNavigation()
  }

  override fun onEpisodesSyncFinished() = viewModel.loadWatchlist()

  override fun onTraktSyncProgress() = viewModel.loadWatchlist()

  private fun render(uiModel: WatchlistMainUiModel) {
    uiModel.run {
      items?.let {
        watchlistMainSearchView.isClickable = it.isNotEmpty() || isSearching == true
        watchlistMainEmptyView.fadeIf(it.isEmpty() && isSearching == false)
      }
    }
  }
}
