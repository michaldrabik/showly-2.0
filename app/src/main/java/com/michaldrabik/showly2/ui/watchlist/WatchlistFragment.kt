package com.michaldrabik.showly2.ui.watchlist

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.model.Tip
import com.michaldrabik.showly2.ui.common.OnEpisodesSyncedListener
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.OnTraktSyncListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment.Companion.ARG_SHOW_ID
import com.michaldrabik.showly2.ui.show.seasons.episodes.details.EpisodeDetailsBottomSheet
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistAdapter
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.showly2.utilities.extensions.fadeIf
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.hideKeyboard
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.showKeyboard
import com.michaldrabik.showly2.utilities.extensions.visible
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import com.michaldrabik.showly2.widget.watchlist.WatchlistWidgetProvider
import kotlinx.android.synthetic.main.fragment_watchlist.*
import kotlinx.android.synthetic.main.layout_watchlist_empty.*
import kotlinx.android.synthetic.main.view_search.*

class WatchlistFragment : BaseFragment<WatchlistViewModel>(R.layout.fragment_watchlist),
  OnTabReselectedListener,
  OnEpisodesSyncedListener,
  OnTraktSyncListener {

  override val viewModel by viewModels<WatchlistViewModel> { viewModelFactory }

  private lateinit var adapter: WatchlistAdapter
  private lateinit var layoutManager: LinearLayoutManager

  override fun onCreate(savedInstanceState: Bundle?) {
    fragmentComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()
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
    watchlistTipItem.onClick {
      it.gone()
      mainActivity().showTip(Tip.WATCHLIST_ITEM_PIN)
    }
    watchlistSearchView.run {
      hint = getString(R.string.textSearchWatchlist)
      settingsIconVisible = true
      isClickable = false
      onClick { enterSearch() }
      onSettingsClickListener = { openSettings() }
    }
  }

  private fun setupRecycler() {
    adapter = WatchlistAdapter()
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    watchlistRecycler.apply {
      adapter = this@WatchlistFragment.adapter
      layoutManager = this@WatchlistFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
    adapter.run {
      itemClickListener = { openShowDetails(it) }
      itemLongClickListener = { item, view -> openPopupMenu(item, view) }
      detailsClickListener = { openEpisodeDetails(it) }
      checkClickListener = { viewModel.setWatchedEpisode(it) }
      missingImageListener = { item, force -> viewModel.findMissingImage(item, force) }
    }
  }

  private fun setupStatusBar() {
    watchlistRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      watchlistRecycler
        .updatePadding(top = statusBarSize + dimenToPx(R.dimen.watchlistRecyclerPadding))
      (watchlistSearchView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.spaceSmall))
    }
  }

  private fun openPopupMenu(item: WatchlistItem, view: View) {
    val menu = PopupMenu(requireContext(), view, Gravity.CENTER)
    if (item.isPinned) {
      menu.inflate(R.menu.watchlist_item_menu_unpin)
    } else {
      menu.inflate(R.menu.watchlist_item_menu_pin)
    }
    menu.setOnMenuItemClickListener { menuItem ->
      if (menuItem.itemId == R.id.menuWatchlistItemPin) {
        viewModel.togglePinItem(item)
      }
      true
    }
    menu.show()
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

  private fun openShowDetails(item: WatchlistItem) {
    exitSearch()
    hideNavigation()
    watchlistRoot.fadeOut {
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, item.show.ids.trakt.id) }
      navigateTo(R.id.actionWatchlistFragmentToShowDetailsFragment, bundle)
    }
  }

  private fun openSettings() {
    hideNavigation()
    navigateTo(R.id.actionWatchlistFragmentToSettingsFragment)
  }

  private fun openEpisodeDetails(item: WatchlistItem) {
    val modal = EpisodeDetailsBottomSheet.create(
      item.show.ids.trakt,
      item.episode,
      isWatched = false,
      showButton = false
    )
    modal.show(requireActivity().supportFragmentManager, "MODAL")
  }

  private fun openTraktSync() {
    navigateTo(R.id.actionWatchlistFragmentToTraktSyncFragment)
    hideNavigation()
  }

  override fun onTabReselected() = watchlistRecycler.smoothScrollToPosition(0)

  override fun onEpisodesSyncFinished() = viewModel.loadWatchlist()

  override fun onTraktSyncProgress() = viewModel.loadWatchlist()

  private fun render(uiModel: WatchlistUiModel) {
    uiModel.run {
      items?.let {
        adapter.setItems(it)
        watchlistRecycler.fadeIn()
        watchlistEmptyView.fadeIf(it.isEmpty() && isSearching == false)
        watchlistSearchView.isClickable = it.isNotEmpty() || isSearching == true
        watchlistTipItem.visibleIf(it.count() >= 3 && !mainActivity().isTipShown(Tip.WATCHLIST_ITEM_PIN))
        WatchlistWidgetProvider.requestUpdate(requireContext())
      }
    }
  }
}
