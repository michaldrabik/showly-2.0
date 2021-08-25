package com.michaldrabik.ui_discover

import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup.MarginLayoutParams
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
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
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
import com.michaldrabik.ui_discover.recycler.DiscoverAdapter
import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import com.michaldrabik.ui_model.Tip
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_discover.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.random.Random

@AndroidEntryPoint
class DiscoverFragment :
  BaseFragment<DiscoverViewModel>(R.layout.fragment_discover),
  OnTabReselectedListener,
  OnTraktSyncListener {

  override val viewModel by viewModels<DiscoverViewModel>()

  private val swipeRefreshStartOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshStartOffset) }
  private val swipeRefreshEndOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshEndOffset) }

  private var adapter: DiscoverAdapter? = null
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
    outState.putFloat("ARG_SEARCH_POS", discoverSearchView?.translationY ?: 0F)
    outState.putFloat("ARG_TABS_POS", discoverModeTabsView?.translationY ?: 0F)
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

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          launch { messageState.collect { showSnack(it) } }
          loadItems()
        }
      }
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
      selectShows()
    }
    discoverMask.onClick { toggleFiltersView() }
    discoverFiltersView.onApplyClickListener = {
      toggleFiltersView()
      viewModel.loadItems(
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
      stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
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
        viewModel.loadItems(pullToRefresh = true)
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
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.collectionTabsMargin))
      discoverTipFilters.translationY = statusBarSize.toFloat()
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

  private fun navigateToSearch() {
    disableUi()
    hideNavigation()
    discoverFiltersView.fadeOut().add(animations)
    discoverModeTabsView.fadeOut(duration = 200).add(animations)
    discoverRecycler.fadeOut(duration = 200) {
      super.navigateTo(R.id.actionDiscoverFragmentToSearchFragment, null)
    }.add(animations)
  }

  private fun navigateToDetails(item: DiscoverListItem) {
    disableUi()
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
        doOnEnd { discoverFiltersView?.invisible() }
        start()
      }.add(animators)
      discoverMask.fadeOut().add(animations)
    }
  }

  private fun render(uiState: DiscoverUiState) {
    uiState.run {
      items?.let {
        val resetScroll = resetScroll?.consume() == true
        adapter?.setItems(it, resetScroll)
        layoutManager?.withSpanSizeLookup { pos -> adapter?.getItems()?.get(pos)?.image?.type?.spanSize!! }
        discoverRecycler.fadeIn()
      }
      isLoading?.let {
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

  override fun onPause() {
    enableUi()
    searchViewPosition = discoverSearchView.translationY
    tabsViewPosition = discoverModeTabsView.translationY
    super.onPause()
  }

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
