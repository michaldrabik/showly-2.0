package com.michaldrabik.ui_discover_movies

import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.utilities.extensions.add
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.disableUi
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.enableUi
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.invisible
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.withSpanSizeLookup
import com.michaldrabik.ui_discover_movies.recycler.DiscoverMovieListItem
import com.michaldrabik.ui_discover_movies.recycler.DiscoverMoviesAdapter
import com.michaldrabik.ui_navigation.java.NavigationArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_discover_movies.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.random.Random

@AndroidEntryPoint
class DiscoverMoviesFragment :
  BaseFragment<DiscoverMoviesViewModel>(R.layout.fragment_discover_movies),
  OnTabReselectedListener,
  OnTraktSyncListener {

  override val viewModel by viewModels<DiscoverMoviesViewModel>()

  private val swipeRefreshStartOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshStartOffset) }
  private val swipeRefreshEndOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshEndOffset) }

  private var adapter: DiscoverMoviesAdapter? = null
  private var layoutManager: GridLayoutManager? = null

  private var searchViewPosition = 0F
  private var tabsViewPosition = 0F

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    savedInstanceState?.let {
      searchViewPosition = it.getFloat("ARG_SEARCH_POS", 0F)
      tabsViewPosition = it.getFloat("ARG_TABS_POS", 0F)
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putFloat("ARG_SEARCH_POS", discoverMoviesSearchView?.translationY ?: 0F)
    outState.putFloat("ARG_TABS_POS", discoverMoviesTabsView?.translationY ?: 0F)
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

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          launch { messageState.collect { showSnack(it) } }
          loadMovies()
        }
      }
    }
  }

  private fun setupView() {
    discoverMoviesSearchView.run {
      sortIconVisible = true
      settingsIconVisible = false
      isClickable = false
      onClick { navigateToSearch() }
      onSortClickListener = { toggleFiltersView() }
      translationY = searchViewPosition
      if (isTraktSyncing()) setTraktProgress(true)
    }
    discoverMoviesTabsView.run {
      translationY = tabsViewPosition
      onModeSelected = { mode = it }
      selectMovies()
    }
    discoverMoviesMask.onClick { toggleFiltersView() }
    discoverMoviesFiltersView.onApplyClickListener = {
      toggleFiltersView()
      viewModel.loadMovies(
        resetScroll = true,
        skipCache = true,
        instantProgress = true,
        newFilters = it
      )
    }
  }

  private fun setupStatusBar() {
    discoverMoviesRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      discoverMoviesRecycler
        .updatePadding(top = statusBarSize + dimenToPx(R.dimen.discoverRecyclerPadding))
      (discoverMoviesSearchView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.spaceSmall))
      (discoverMoviesFiltersView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.searchViewHeight))
      (discoverMoviesTabsView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.collectionTabsMargin))
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
      itemClickListener = { navigateToDetails(it) },
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

  private fun navigateToSearch() {
    disableUi()
    hideNavigation()
    discoverMoviesFiltersView.fadeOut().add(animations)
    discoverMoviesTabsView.fadeOut(duration = 200).add(animations)
    discoverMoviesRecycler.fadeOut(duration = 200) {
      super.navigateTo(R.id.actionDiscoverMoviesFragmentToSearchFragment, null)
    }.add(animations)
  }

  private fun navigateToDetails(item: DiscoverMovieListItem) {
    disableUi()
    hideNavigation()
    animateItemsExit(item)
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
        navigateTo(R.id.actionDiscoverMoviesFragmentToMovieDetailsFragment, bundle)
      }
    ).add(animations)
  }

  private fun toggleFiltersView() {
    val delta = dimenToPx(R.dimen.searchViewHeight)
    val cx = discoverMoviesFiltersView.width
    val cy = discoverMoviesFiltersView.height + dimenToPx(R.dimen.searchViewHeight)
    val radius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
    if (!discoverMoviesFiltersView.isVisible) {
      val anim = ViewAnimationUtils.createCircularReveal(discoverMoviesFiltersView, cx, -delta, 0F, radius)
      discoverMoviesFiltersView.visible()
      discoverMoviesMask.fadeIn()
      anim.start()
    } else {
      ViewAnimationUtils.createCircularReveal(discoverMoviesFiltersView, cx, -delta, radius, 0F).apply {
        doOnEnd { discoverMoviesFiltersView?.invisible() }
        start()
      }.add(animators)
      discoverMoviesMask.fadeOut().add(animations)
    }
  }

  private fun render(uiState: DiscoverMoviesUiState) {
    uiState.run {
      items?.let {
        val resetScroll = resetScroll?.consume() == true
        adapter?.setItems(it, resetScroll)
        layoutManager?.withSpanSizeLookup { pos -> adapter?.getItems()?.get(pos)?.image?.type?.spanSize!! }
        discoverMoviesRecycler.fadeIn()
      }
      isLoading?.let {
        discoverMoviesSearchView.isClickable = !it
        discoverMoviesSearchView.sortIconClickable = !it
        discoverMoviesSearchView.isEnabled = !it
        discoverMoviesSwipeRefresh.isRefreshing = it
        discoverMoviesTabsView.isEnabled = !it
      }
      filters?.let {
        discoverMoviesFiltersView.run {
          if (!this.isVisible) bind(it)
        }
        discoverMoviesSearchView.iconBadgeVisible = !it.isDefault()
      }
    }
  }

  override fun onTraktSyncProgress() = discoverMoviesSearchView.setTraktProgress(true)

  override fun onTraktSyncComplete() = discoverMoviesSearchView.setTraktProgress(false)

  override fun onTabReselected() = navigateToSearch()

  override fun onPause() {
    enableUi()
    searchViewPosition = discoverMoviesSearchView.translationY
    tabsViewPosition = discoverMoviesTabsView.translationY
    super.onPause()
  }

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
