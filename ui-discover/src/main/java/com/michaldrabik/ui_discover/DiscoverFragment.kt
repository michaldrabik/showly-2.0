package com.michaldrabik.ui_discover

import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.common.Config.MAIN_GRID_SPAN
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.utilities.extensions.add
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.disableUi
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.enableUi
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.invisible
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withSpanSizeLookup
import com.michaldrabik.ui_discover.di.UiDiscoverComponentProvider
import com.michaldrabik.ui_discover.recycler.DiscoverAdapter
import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import com.michaldrabik.ui_model.Tip
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import kotlinx.android.synthetic.main.fragment_discover.*
import kotlin.math.hypot
import kotlin.random.Random

class DiscoverFragment :
  BaseFragment<DiscoverViewModel>(R.layout.fragment_discover),
  OnTabReselectedListener,
  OnTraktSyncListener {

  override val viewModel by viewModels<DiscoverViewModel> { viewModelFactory }

  private val swipeRefreshStartOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshStartOffset) }
  private val swipeRefreshEndOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshEndOffset) }

  private var adapter: DiscoverAdapter? = null
  private var layoutManager: GridLayoutManager? = null

  private var searchViewPosition = 0F
  private var tabsViewPosition = 0F

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiDiscoverComponentProvider).provideDiscoverComponent().inject(this)
    super.onCreate(savedInstanceState)
    if (!isInitialized) {
      isInitialized = true
      savedInstanceState?.let {
        searchViewPosition = it.getFloat("ARG_DISCOVER_SEARCH_POS", 0F)
        tabsViewPosition = it.getFloat("ARG_DISCOVER_TABS_POS", 0F)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
  }

  override fun onPause() {
    enableUi()
    super.onPause()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()
    setupSwipeRefresh()
    setupStatusBar()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
      messageLiveData.observe(viewLifecycleOwner, { showSnack(it) })
      loadDiscoverShows()
    }
  }

  private fun setupView() {
    discoverSearchView.run {
      sortIconVisible = true
      settingsIconVisible = false
      isClickable = false
      onClick { navigateToSearch() }
      onSortClickListener = { toggleFiltersView() }
      translationY = searchViewPosition
      if (isTraktSyncing()) setTraktProgress(true)
    }
    discoverModeTabsView.run {
      visibleIf(moviesEnabled)
      translationY = tabsViewPosition
      onModeSelected = { mode = it }
      animateShows()
    }
    discoverMask.onClick { toggleFiltersView() }
    discoverFiltersView.onApplyClickListener = {
      toggleFiltersView()
      viewModel.loadDiscoverShows(
        scrollToTop = true,
        skipCache = true,
        instantProgress = true,
        newFilters = it
      )
    }
    discoverTipFilters.run {
      fadeIf(!isTipShown(Tip.DISCOVER_FILTERS))
      onClick {
        it.gone()
        showTip(Tip.DISCOVER_FILTERS)
      }
    }
  }

  private fun setupRecycler() {
    layoutManager = GridLayoutManager(context, MAIN_GRID_SPAN)
    adapter = DiscoverAdapter().apply {
      missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
      itemClickListener = { navigateToDetails(it) }
      listChangeListener = { discoverRecycler.scrollToPosition(0) }
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
        viewModel.loadDiscoverShows(pullToRefresh = true)
      }
    }
  }

  private fun setupStatusBar() {
    discoverRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      val recyclerPadding =
        if (moviesEnabled) R.dimen.discoverRecyclerPadding
        else R.dimen.discoverRecyclerPaddingNoTabs

      discoverRecycler
        .updatePadding(top = statusBarSize + dimenToPx(recyclerPadding))
      (discoverSearchView.layoutParams as MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.spaceSmall))
      (discoverFiltersView.layoutParams as MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.searchViewHeight))
      (discoverModeTabsView.layoutParams as MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.showsMoviesTabsMargin))
      discoverTipFilters.translationY = statusBarSize.toFloat()
      discoverSwipeRefresh.setProgressViewOffset(
        true,
        swipeRefreshStartOffset + statusBarSize,
        swipeRefreshEndOffset
      )
    }
  }

  private fun navigateToSearch() {
    disableUi()
    saveUi()
    hideNavigation()
    discoverFiltersView.fadeOut().add(animations)
    discoverModeTabsView.fadeOut(duration = 200).add(animations)
    discoverRecycler.fadeOut(duration = 200) {
      super.navigateTo(R.id.actionDiscoverFragmentToSearchFragment, null)
    }.add(animations)
  }

  private fun navigateToDetails(item: DiscoverListItem) {
    disableUi()
    saveUi()
    hideNavigation()
    animateItemsExit(item)
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
        navigateTo(R.id.actionDiscoverFragmentToShowDetailsFragment, bundle)
      }
    ).add(animations)
  }

  private fun toggleFiltersView() {
    val delta = dimenToPx(R.dimen.searchViewHeight)
    val cx = discoverFiltersView.width
    val cy = discoverFiltersView.height + dimenToPx(R.dimen.searchViewHeight)
    val radius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
    if (!discoverFiltersView.isVisible) {
      val anim = ViewAnimationUtils.createCircularReveal(discoverFiltersView, cx, -delta, 0F, radius)
      discoverFiltersView.visible()
      discoverMask.fadeIn()
      anim.start()
    } else {
      ViewAnimationUtils.createCircularReveal(discoverFiltersView, cx, -delta, radius, 0F).apply {
        doOnEnd { discoverFiltersView.invisible() }
        start()
      }
      discoverMask.fadeOut()
    }
  }

  private fun saveUi() {
    searchViewPosition = discoverSearchView.translationY
    tabsViewPosition = discoverModeTabsView.translationY
  }

  private fun render(uiModel: DiscoverUiModel) {
    uiModel.run {
      shows?.let {
        adapter?.setItems(it, scrollToTop == true)
        layoutManager?.withSpanSizeLookup { pos -> adapter?.getItems()?.get(pos)?.image?.type?.spanSize!! }
        discoverRecycler.fadeIn()
      }
      showLoading?.let {
        discoverSearchView.isClickable = !it
        discoverSearchView.sortIconClickable = !it
        discoverSearchView.isEnabled = !it
        discoverSwipeRefresh.isRefreshing = it
        discoverModeTabsView.isEnabled = !it
      }
      filters?.let {
        discoverFiltersView.run {
          if (!this.isVisible) bind(it)
        }
        discoverSearchView.iconBadgeVisible = !it.isDefault()
      }
    }
  }

  override fun onTraktSyncProgress() = discoverSearchView.setTraktProgress(true)

  override fun onTraktSyncComplete() = discoverSearchView.setTraktProgress(false)

  override fun onTabReselected() = navigateToSearch()

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putFloat("ARG_DISCOVER_SEARCH_POS", searchViewPosition)
    outState.putFloat("ARG_DISCOVER_TABS_POS", tabsViewPosition)
  }

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
