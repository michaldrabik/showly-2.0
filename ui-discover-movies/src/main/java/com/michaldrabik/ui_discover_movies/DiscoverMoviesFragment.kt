package com.michaldrabik.ui_discover_movies

import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.common.Config
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
import com.michaldrabik.ui_base.utilities.extensions.withSpanSizeLookup
import com.michaldrabik.ui_discover_movies.recycler.DiscoverMovieListItem
import com.michaldrabik.ui_discover_movies.recycler.DiscoverMoviesAdapter
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_navigation.java.NavigationArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_discover_movies.*
import kotlin.random.Random

@AndroidEntryPoint
class DiscoverMoviesFragment :
  BaseFragment<DiscoverMoviesViewModel>(R.layout.fragment_discover_movies),
  OnTabReselectedListener {

  companion object {
    const val REQUEST_DISCOVER_FILTERS = "REQUEST_DISCOVER_FILTERS"
  }

  override val viewModel by viewModels<DiscoverMoviesViewModel>()
  override val navigationId = R.id.discoverMoviesFragment

  private val swipeRefreshStartOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshStartOffset) }
  private val swipeRefreshEndOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshEndOffset) }

  private var adapter: DiscoverMoviesAdapter? = null
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
    outState.putFloat("ARG_SEARCH_POS", discoverMoviesSearchView?.translationY ?: 0F)
    outState.putFloat("ARG_TABS_POS", discoverMoviesTabsView?.translationY ?: 0F)
    outState.putFloat("ARG_FILTERS_POS", discoverMoviesFiltersView?.translationY ?: 0F)
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupStatusBar()
    setupRecycler()
    setupSwipeRefresh()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.messageFlow.collect { showSnack(it) } },
      doAfterLaunch = { viewModel.loadMovies() }
    )

    setFragmentResultListener(REQUEST_DISCOVER_FILTERS) { _, _ ->
      viewModel.loadMovies(resetScroll = true, skipCache = true, instantProgress = true)
    }
  }

  private fun setupView() {
    discoverMoviesSearchView.run {
      translationY = searchViewPosition
      settingsIconVisible = true
      isEnabled = false
      onClick { openSearch() }
      onSettingsClickListener = {
        hideNavigation()
        navigateToSafe(R.id.actionDiscoverMoviesFragmentToSettingsFragment)
      }
    }
    discoverMoviesTabsView.run {
      translationY = tabsViewPosition
      onModeSelected = { mode = it }
      selectMovies()
    }
    discoverMoviesFiltersView.run {
      translationY = filtersViewPosition
      onGenresChipClick = { navigateToSafe(R.id.actionDiscoverMoviesFragmentToFiltersGenres) }
      onFeedChipClick = { navigateToSafe(R.id.actionDiscoverMoviesFragmentToFiltersFeed) }
      onHideAnticipatedChipClick = { viewModel.toggleAnticipated() }
      onHideCollectionChipClick = { viewModel.toggleCollection() }
    }
  }

  private fun setupStatusBar() {
    discoverMoviesRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      discoverMoviesRecycler
        .updatePadding(top = statusBarSize + dimenToPx(R.dimen.discoverRecyclerPadding))
      (discoverMoviesSearchView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.spaceMedium))
      (discoverMoviesTabsView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.collectionTabsMargin))
      (discoverMoviesFiltersView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.collectionFiltersMargin))
      discoverMoviesSwipeRefresh.setProgressViewOffset(
        true,
        swipeRefreshStartOffset + statusBarSize,
        swipeRefreshEndOffset
      )
    }
  }

  private fun setupRecycler() {
    layoutManager = GridLayoutManager(context, Config.MAIN_GRID_SPAN)
    adapter = DiscoverMoviesAdapter(
      itemClickListener = {
        when (it.image.type) {
          ImageType.PREMIUM -> openPremium()
          else -> openDetails(it)
        }
      },
      itemLongClickListener = { openMovieMenu(it.movie) },
      missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) },
      listChangeListener = { discoverMoviesRecycler.scrollToPosition(0) }
    )
    discoverMoviesRecycler.apply {
      adapter = this@DiscoverMoviesFragment.adapter
      layoutManager = this@DiscoverMoviesFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
  }

  private fun setupSwipeRefresh() {
    discoverMoviesSwipeRefresh.apply {
      val color = requireContext().colorFromAttr(R.attr.colorAccent)
      setProgressBackgroundColorSchemeColor(requireContext().colorFromAttr(R.attr.colorSearchViewBackground))
      setColorSchemeColors(color, color, color)
      setOnRefreshListener {
        searchViewPosition = 0F
        tabsViewPosition = 0F
        viewModel.loadMovies(pullToRefresh = true)
      }
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
    discoverMoviesTabsView.fadeOut(duration = 200).add(animations)
    discoverMoviesFiltersView.fadeOut(duration = 200).add(animations)
    discoverMoviesRecycler.fadeOut(duration = 200) {
      super.navigateTo(R.id.actionDiscoverMoviesFragmentToSearchFragment, null)
    }.add(animations)
  }

  private fun openDetails(item: DiscoverMovieListItem) {
    if (discoverMoviesRecycler?.isEnabled == false) return
    disableUi()
    hideNavigation()
    animateItemsExit(item)
  }

  private fun openMovieMenu(movie: Movie) {
    if (discoverMoviesRecycler?.isEnabled == false) return
    setFragmentResultListener(NavigationArgs.REQUEST_ITEM_MENU) { requestKey, _ ->
      if (requestKey == NavigationArgs.REQUEST_ITEM_MENU) {
        viewModel.loadMovies()
      }
      clearFragmentResultListener(NavigationArgs.REQUEST_ITEM_MENU)
    }
    val bundle = ContextMenuBottomSheet.createBundle(movie.ids.trakt)
    navigateToSafe(R.id.actionDiscoverMoviesFragmentToItemMenu, bundle)
  }

  private fun openPremium() {
    if (discoverMoviesRecycler?.isEnabled == false) return
    disableUi()
    hideNavigation()
    navigateToSafe(R.id.actionDiscoverMoviesFragmentToPremium, Bundle.EMPTY)
  }

  private fun animateItemsExit(item: DiscoverMovieListItem) {
    discoverMoviesSearchView.fadeOut().add(animations)
    discoverMoviesTabsView.fadeOut().add(animations)
    discoverMoviesFiltersView.fadeOut().add(animations)

    val clickedIndex = adapter?.indexOf(item) ?: 0
    val itemCount = adapter?.itemCount ?: 0
    (0..itemCount).forEach {
      if (it != clickedIndex) {
        val view = discoverMoviesRecycler.findViewHolderForAdapterPosition(it)
        view?.let { v ->
          val randomDelay = Random.nextLong(50, 200)
          v.itemView.fadeOut(duration = 150, startDelay = randomDelay).add(animations)
        }
      }
    }

    val clickedView = discoverMoviesRecycler.findViewHolderForAdapterPosition(clickedIndex)
    clickedView?.itemView?.fadeOut(
      duration = 150, startDelay = 350,
      endAction = {
        if (!isResumed) return@fadeOut
        val bundle = Bundle().apply { putLong(NavigationArgs.ARG_MOVIE_ID, item.movie.traktId) }
        navigateToSafe(R.id.actionDiscoverMoviesFragmentToMovieDetailsFragment, bundle)
      }
    ).add(animations)
  }

  private fun render(uiState: DiscoverMoviesUiState) {
    uiState.run {
      items?.let {
        val resetScroll = resetScroll?.consume() == true
        adapter?.setItems(it, resetScroll)
        layoutManager?.withSpanSizeLookup { pos -> adapter?.getItems()?.get(pos)?.image?.type?.spanSize!! }
        discoverMoviesRecycler.fadeIn()
      }
      isSyncing?.let {
        discoverMoviesSearchView.setTraktProgress(it)
        discoverMoviesSearchView.isEnabled = !it
      }
      isLoading?.let {
        discoverMoviesSwipeRefresh.isRefreshing = it
        discoverMoviesSearchView.isEnabled = !it
        discoverMoviesSearchView.sortIconClickable = !it
        discoverMoviesTabsView.isEnabled = !it
        discoverMoviesFiltersView.isEnabled = !it
        discoverMoviesRecycler.isEnabled = !it
      }
      filters?.let {
        if (discoverMoviesFiltersView.visibility != VISIBLE) {
          discoverMoviesFiltersView.fadeIn(duration = 300)
        }
        discoverMoviesFiltersView.bind(it)
      }
    }
  }

  override fun onTabReselected() = openSearch()

  override fun onPause() {
    enableUi()
    searchViewPosition = discoverMoviesSearchView.translationY
    tabsViewPosition = discoverMoviesTabsView.translationY
    filtersViewPosition = discoverMoviesFiltersView.translationY
    super.onPause()
  }

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
