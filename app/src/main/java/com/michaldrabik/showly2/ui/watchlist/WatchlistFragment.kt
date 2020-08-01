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
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.ui.common.OnEpisodesSyncedListener
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.OnTraktSyncListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment
import com.michaldrabik.showly2.ui.show.seasons.episodes.details.EpisodeDetailsBottomSheet
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.hideKeyboard
import com.michaldrabik.showly2.utilities.extensions.nextPage
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.showKeyboard
import com.michaldrabik.showly2.utilities.extensions.visible
import kotlinx.android.synthetic.main.fragment_watchlist.*
import kotlinx.android.synthetic.main.view_search.*

class WatchlistFragment : BaseFragment<WatchlistViewModel>(R.layout.fragment_watchlist),
  OnEpisodesSyncedListener,
  OnTabReselectedListener,
  OnTraktSyncListener {

  override val viewModel by viewModels<WatchlistViewModel> { viewModelFactory }

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
    watchlistSearchView.run {
      hint = getString(R.string.textSearchWatchlist)
      settingsIconVisible = true
      isClickable = false
      onClick { enterSearch() }
      onSettingsClickListener = { openSettings() }
    }

    watchlistTabs.translationY = tabsTranslation
    watchlistSearchView.translationY = searchViewTranslation
  }

  @SuppressLint("WrongConstant")
  private fun setupPager() {
    watchlistPager.run {
      offscreenPageLimit = WatchlistPagesAdapter.PAGES_COUNT
      isUserInputEnabled = false
      adapter = WatchlistPagesAdapter(this@WatchlistFragment)
    }

    watchlistTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
      override fun onTabSelected(tab: TabLayout.Tab) {
        watchlistPager.currentItem = tab.position
      }

      override fun onTabReselected(tab: TabLayout.Tab) = Unit
      override fun onTabUnselected(tab: TabLayout.Tab) = Unit
    })
    TabLayoutMediator(watchlistTabs, watchlistPager) { tab, position ->
      tab.text = when (position) {
        0 -> getString(R.string.tabWatchlist)
        else -> getString(R.string.tabCalendar)
      }
    }.attach()
  }

  private fun setupStatusBar() {
    watchlistMainRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      (watchlistSearchView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.spaceSmall))
      (watchlistTabs.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.watchlistSearchViewPadding))
    }
  }

  fun openShowDetails(item: WatchlistItem) {
    exitSearch()
    hideNavigation()
    saveUiTranslations()
    watchlistMainRoot.fadeOut {
      val bundle = Bundle().apply { putLong(ShowDetailsFragment.ARG_SHOW_ID, item.show.ids.trakt.id) }
      navigateTo(R.id.actionWatchlistFragmentToShowDetailsFragment, bundle)
    }
  }

  private fun openSettings() {
    hideNavigation()
    navigateTo(R.id.actionWatchlistFragmentToSettingsFragment)
    saveUiTranslations()
  }

  fun openTraktSync() {
    navigateTo(R.id.actionWatchlistFragmentToTraktSyncFragment)
    hideNavigation()
    saveUiTranslations()
  }

  fun openEpisodeDetails(showId: IdTrakt, episode: Episode) {
    val modal = EpisodeDetailsBottomSheet.create(
      showId,
      episode,
      isWatched = false,
      showButton = false
    )
    modal.show(requireActivity().supportFragmentManager, "MODAL")
  }

  private fun saveUiTranslations() {
    tabsTranslation = watchlistTabs.translationY
    searchViewTranslation = watchlistSearchView.translationY
  }

  private fun enterSearch() {
    if (watchlistSearchView.isSearching) return
    watchlistSearchView.isSearching = true
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
    watchlistSearchView.isSearching = false
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

  override fun onTabReselected() {
    watchlistSearchView.translationY = 0F
    watchlistTabs.translationY = 0F
    watchlistPager.nextPage()
    childFragmentManager.fragments.forEach {
      (it as? OnTabReselectedListener)?.onTabReselected()
    }
  }

  private fun render(uiModel: WatchlistUiModel) {
    uiModel.run {
      items?.let {
        watchlistSearchView.isClickable = it.isNotEmpty() || isSearching == true
      }
    }
  }
}
