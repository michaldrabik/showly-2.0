package com.michaldrabik.showly2.ui.watchlist

import android.annotation.SuppressLint
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.updateMargins
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.ui.common.OnEpisodesSyncedListener
import com.michaldrabik.showly2.ui.common.OnScrollResetListener
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.OnTraktSyncListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
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
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.common.views.exSearchViewIcon
import com.michaldrabik.ui_base.common.views.exSearchViewInput
import com.michaldrabik.ui_base.common.views.exSearchViewText
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.EPISODES_LEFT
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.RECENTLY_WATCHED
import com.michaldrabik.ui_show.ShowDetailsFragment
import com.michaldrabik.ui_show.episode_details.EpisodeDetailsBottomSheet
import kotlinx.android.synthetic.main.fragment_watchlist.*

class WatchlistFragment : BaseFragment<WatchlistViewModel>(R.layout.fragment_watchlist),
  OnEpisodesSyncedListener,
  OnTabReselectedListener,
  OnTraktSyncListener, TabLayout.OnTabSelectedListener {

  override val viewModel by viewModels<WatchlistViewModel> { viewModelFactory }

  private var searchViewTranslation = 0F
  private var tabsTranslation = 0F
  private var sortIconTranslation = 0F

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
      uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
      messageLiveData.observe(viewLifecycleOwner, { showSnack(it) })
    }
  }

  override fun onResume() {
    super.onResume()
    setupBackPressed()
    showNavigation()
    viewModel.loadWatchlist()
  }

  override fun onDestroyView() {
    watchlistTabs.removeOnTabSelectedListener(this)
    super.onDestroyView()
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
    watchlistSortIcon.translationY = sortIconTranslation
  }

  @SuppressLint("WrongConstant")
  private fun setupPager() {
    watchlistPager.run {
      offscreenPageLimit = WatchlistPagesAdapter.PAGES_COUNT
      isUserInputEnabled = false
      adapter = WatchlistPagesAdapter(this@WatchlistFragment)
    }

    TabLayoutMediator(watchlistTabs, watchlistPager) { tab, position ->
      tab.text = when (position) {
        0 -> getString(R.string.tabWatchlist)
        else -> getString(R.string.tabCalendar)
      }
    }.attach()

    watchlistTabs.addOnTabSelectedListener(this)
  }

  private fun setupStatusBar() {
    watchlistMainRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      (watchlistSearchView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.spaceSmall))
      (watchlistTabs.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.watchlistSearchViewPadding))
      (watchlistSortIcon.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.watchlistSearchViewPadding))
    }
  }

  private fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      if (watchlistSearchView.isSearching) {
        exitSearch()
      } else {
        remove()
        dispatcher.onBackPressed()
      }
    }
  }

  override fun onTabSelected(tab: TabLayout.Tab) {
    watchlistPager.currentItem = tab.position
  }

  fun openShowDetails(item: WatchlistItem) {
    viewModel.onOpenShowDetails()
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

  private fun openSortOrderDialog(order: SortOrder) {
    val options = listOf(NAME, RECENTLY_WATCHED, EPISODES_LEFT)
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

  private fun saveUiTranslations() {
    tabsTranslation = watchlistTabs.translationY
    searchViewTranslation = watchlistSearchView.translationY
    sortIconTranslation = watchlistSortIcon.translationY
  }

  private fun enterSearch() {
    if (watchlistSearchView.isSearching) return
    watchlistSearchView.isSearching = true
    exSearchViewText.gone()
    exSearchViewInput.run {
      setText("")
      doAfterTextChanged { viewModel.searchWatchlist(it?.toString() ?: "") }
      visible()
      showKeyboard()
      requestFocus()
    }
    (exSearchViewIcon.drawable as Animatable).start()
    exSearchViewIcon.onClick { exitSearch() }
    hideNavigation(false)
  }

  private fun exitSearch(showNavigation: Boolean = true) {
    watchlistSearchView.isSearching = false
    exSearchViewText.visible()
    exSearchViewInput.run {
      setText("")
      gone()
      hideKeyboard()
      clearFocus()
    }
    exSearchViewIcon.setImageResource(R.drawable.ic_anim_search_to_close)
    if (showNavigation) showNavigation()
  }

  override fun onEpisodesSyncFinished() = viewModel.loadWatchlist()

  override fun onTraktSyncProgress() = viewModel.loadWatchlist()

  override fun onTabReselected() {
    watchlistSearchView.translationY = 0F
    watchlistTabs.translationY = 0F
    watchlistSortIcon.translationY = 0F
    watchlistPager.nextPage()
    childFragmentManager.fragments.forEach {
      (it as? OnScrollResetListener)?.onScrollReset()
    }
  }

  fun resetTranslations() {
    watchlistSearchView.animate().translationY(0F).start()
    watchlistTabs.animate().translationY(0F).start()
    watchlistSortIcon.animate().translationY(0F).start()
  }

  private fun render(uiModel: WatchlistUiModel) {
    uiModel.run {
      items?.let {
        watchlistSearchView.isClickable = it.isNotEmpty() || isSearching == true
        watchlistSortIcon.visibleIf(it.isNotEmpty())
        if (it.isNotEmpty() && sortOrder != null) {
          watchlistSortIcon.onClick { openSortOrderDialog(sortOrder) }
        }
      }
    }
  }

  override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
  override fun onTabReselected(tab: TabLayout.Tab?) = Unit
}
