package com.michaldrabik.ui_discover

import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.addCallback
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.common.Config
import com.michaldrabik.common.Config.MAIN_GRID_SPAN
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.common.sheets.context_menu.ContextMenuBottomSheet
import com.michaldrabik.ui_base.utilities.extensions.add
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.disableUi
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.enableUi
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.navigateToSafe
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.openWebUrl
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withSpanSizeLookup
import com.michaldrabik.ui_discover.recycler.DiscoverAdapter
import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_ITEM_MENU
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_discover.*
import kotlin.random.Random

@AndroidEntryPoint
class DiscoverFragment :
  BaseFragment<DiscoverViewModel>(R.layout.fragment_discover),
  OnTabReselectedListener {

  companion object {
    const val REQUEST_DISCOVER_FILTERS = "REQUEST_DISCOVER_FILTERS"
  }

  override val viewModel by viewModels<DiscoverViewModel>()
  override val navigationId = R.id.discoverFragment

  private val swipeRefreshStartOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshStartOffset) }
  private val swipeRefreshEndOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshEndOffset) }

  private var adapter: DiscoverAdapter? = null
  private var layoutManager: GridLayoutManager? = null

  private var searchViewPosition = 0F
  private var tabsViewPosition = 0F
  private var filtersViewPosition = 0F

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    savedInstanceState?.let {
      searchViewPosition = it.getFloat("ARG_SEARCH_POS", 0F)
      tabsViewPosition = it.getFloat("ARG_TABS_POS", 0F)
      filtersViewPosition = it.getFloat("ARG_FILTERS_POS", 0F)
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putFloat("ARG_SEARCH_POS", discoverSearchView?.translationY ?: 0F)
    outState.putFloat("ARG_TABS_POS", discoverModeTabsView?.translationY ?: 0F)
    outState.putFloat("ARG_FILTERS_POS", discoverFiltersView?.translationY ?: 0F)
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()
    setupSwipeRefresh()
    setupStatusBar()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.messageFlow.collect { showSnack(it) } },
      doAfterLaunch = { viewModel.loadShows() }
    )

    setFragmentResultListener(REQUEST_DISCOVER_FILTERS) { _, _ ->
      viewModel.loadShows(scrollToTop = true, skipCache = true, instantProgress = true)
    }
  }

  private fun setupView() {
    discoverSearchView.run {
      settingsIconVisible = true
      isEnabled = false
      onClick { openSearch() }
      onSettingsClickListener = {
        hideNavigation()
        navigateToSafe(R.id.actionDiscoverFragmentToSettingsFragment)
      }
      translationY = searchViewPosition
    }
    discoverModeTabsView.run {
      visibleIf(moviesEnabled)
      translationY = tabsViewPosition
      onModeSelected = { mode = it }
      selectShows()
    }
    discoverFiltersView.run {
      translationY = filtersViewPosition
      onGenresChipClick = { navigateToSafe(R.id.actionDiscoverFragmentToFiltersGenres) }
      onNetworksChipClick = { navigateToSafe(R.id.actionDiscoverFragmentToFiltersNetworks) }
      onFeedChipClick = { navigateToSafe(R.id.actionDiscoverFragmentToFiltersFeed) }
      onHideAnticipatedChipClick = { viewModel.toggleAnticipated() }
      onHideCollectionChipClick = { viewModel.toggleCollection() }
    }
  }

  private fun setupRecycler() {
    layoutManager = GridLayoutManager(context, MAIN_GRID_SPAN)
    adapter = DiscoverAdapter(
      itemClickListener = {
        when (it.image.type) {
          ImageType.TWITTER -> openWebUrl(Config.TWITTER_URL)
          ImageType.PREMIUM -> openPremium()
          else -> openDetails(it)
        }
      },
      itemLongClickListener = { item -> openShowMenu(item.show) },
      missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) },
      listChangeListener = { discoverRecycler.scrollToPosition(0) },
      twitterCancelClickListener = { viewModel.cancelTwitterAd() }
    ).apply {
      stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }
    discoverRecycler.apply {
      adapter = this@DiscoverFragment.adapter
      layoutManager = this@DiscoverFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
  }

  private fun setupSwipeRefresh() {
    discoverSwipeRefresh.apply {
      val color = requireContext().colorFromAttr(R.attr.colorAccent)
      setProgressBackgroundColorSchemeColor(requireContext().colorFromAttr(R.attr.colorSearchViewBackground))
      setColorSchemeColors(color, color, color)
      setOnRefreshListener {
        searchViewPosition = 0F
        tabsViewPosition = 0F
        viewModel.loadShows(pullToRefresh = true)
      }
    }
  }

  private fun setupStatusBar() {
    discoverRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      val recyclerPadding =
        if (moviesEnabled) R.dimen.discoverRecyclerPadding
        else R.dimen.discoverRecyclerPaddingNoTabs

      val filtersPadding =
        if (moviesEnabled) R.dimen.collectionFiltersMargin
        else R.dimen.collectionFiltersMarginNoTabs

      discoverRecycler
        .updatePadding(top = statusBarSize + dimenToPx(recyclerPadding))
      (discoverSearchView.layoutParams as MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.spaceMedium))
      (discoverModeTabsView.layoutParams as MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.collectionTabsMargin))
      (discoverFiltersView.layoutParams as MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(filtersPadding))
      discoverSwipeRefresh.setProgressViewOffset(
        true,
        swipeRefreshStartOffset + statusBarSize,
        swipeRefreshEndOffset
      )
    }
  }

  override fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      isEnabled = false
      activity?.onBackPressed()
    }
  }

  private fun openSearch() {
    disableUi()
    hideNavigation()
    discoverModeTabsView.fadeOut(duration = 200).add(animations)
    discoverFiltersView.fadeOut(duration = 200).add(animations)
    discoverRecycler.fadeOut(duration = 200) {
      super.navigateTo(R.id.actionDiscoverFragmentToSearchFragment, null)
    }.add(animations)
  }

  private fun openDetails(item: DiscoverListItem) {
    if (discoverRecycler?.isEnabled == false) return
    disableUi()
    hideNavigation()
    animateItemsExit(item)
  }

  private fun openPremium() {
    if (discoverRecycler?.isEnabled == false) return
    disableUi()
    hideNavigation()
    navigateToSafe(R.id.actionDiscoverFragmentToPremium, Bundle.EMPTY)
  }

  private fun openShowMenu(show: Show) {
    if (discoverRecycler?.isEnabled == false) return
    setFragmentResultListener(REQUEST_ITEM_MENU) { requestKey, _ ->
      if (requestKey == REQUEST_ITEM_MENU) {
        viewModel.loadShows()
      }
      clearFragmentResultListener(REQUEST_ITEM_MENU)
    }
    val bundle = ContextMenuBottomSheet.createBundle(show.ids.trakt)
    navigateToSafe(R.id.actionDiscoverFragmentToItemMenu, bundle)
  }

  private fun animateItemsExit(item: DiscoverListItem) {
    discoverSearchView.fadeOut().add(animations)
    discoverModeTabsView.fadeOut().add(animations)
    discoverFiltersView.fadeOut().add(animations)

    val clickedIndex = adapter?.indexOf(item) ?: 0
    val itemsCount = adapter?.itemCount ?: 0
    (0..itemsCount).forEach {
      if (it != clickedIndex) {
        val view = discoverRecycler.findViewHolderForAdapterPosition(it)
        view?.let { v ->
          val randomDelay = Random.nextLong(50, 200)
          v.itemView.fadeOut(duration = 150, startDelay = randomDelay).add(animations)
        }
      }
    }

    val clickedView = discoverRecycler.findViewHolderForAdapterPosition(clickedIndex)
    clickedView?.itemView?.fadeOut(
      duration = 150, startDelay = 350,
      endAction = {
        if (!isResumed) return@fadeOut
        val bundle = Bundle().apply { putLong(ARG_SHOW_ID, item.show.traktId) }
        navigateToSafe(R.id.actionDiscoverFragmentToShowDetailsFragment, bundle)
      }
    ).add(animations)
  }

  private fun render(uiState: DiscoverUiState) {
    uiState.run {
      items?.let {
        val resetScroll = resetScroll?.consume() == true
        adapter?.setItems(it, resetScroll)
        layoutManager?.withSpanSizeLookup { pos -> adapter?.getItems()?.get(pos)?.image?.type?.spanSize!! }
        discoverRecycler.fadeIn()
      }
      isSyncing?.let {
        discoverSearchView.setTraktProgress(it)
        discoverSearchView.isEnabled = !it
      }
      isLoading?.let {
        discoverSearchView.sortIconClickable = !it
        discoverSearchView.isEnabled = !it
        discoverSwipeRefresh.isRefreshing = it
        discoverModeTabsView.isEnabled = !it
        discoverFiltersView.isEnabled = !it
        discoverRecycler.isEnabled = !it
      }
      filters?.let {
        if (discoverFiltersView.visibility != View.VISIBLE) {
          discoverFiltersView.fadeIn(duration = 300)
        }
        discoverFiltersView.bind(it)
      }
    }
  }

  override fun onTabReselected() = openSearch()

  override fun onPause() {
    enableUi()
    searchViewPosition = discoverSearchView.translationY
    tabsViewPosition = discoverModeTabsView.translationY
    filtersViewPosition = discoverFiltersView.translationY
    super.onPause()
  }

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
