package com.michaldrabik.showly2.ui.discover

import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.model.Tip.DISCOVER_FILTERS
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverAdapter
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment.Companion.ARG_SHOW_ID
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.disableUi
import com.michaldrabik.showly2.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.showly2.utilities.extensions.enableUi
import com.michaldrabik.showly2.utilities.extensions.fadeIf
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.invisible
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visible
import com.michaldrabik.showly2.utilities.extensions.withSpanSizeLookup
import kotlinx.android.synthetic.main.fragment_discover.*
import kotlin.math.hypot
import kotlin.random.Random

class DiscoverFragment : BaseFragment<DiscoverViewModel>(R.layout.fragment_discover), OnTabReselectedListener {

  override val viewModel by viewModels<DiscoverViewModel> { viewModelFactory }

  private val gridSpan by lazy { resources.getInteger(R.integer.discoverGridSpan) }
  private val swipeRefreshStartOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshStartOffset) }
  private val swipeRefreshEndOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshEndOffset) }

  private lateinit var adapter: DiscoverAdapter
  private lateinit var layoutManager: GridLayoutManager

  override fun onCreate(savedInstanceState: Bundle?) {
    fragmentComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()
    setupSwipeRefresh()
    setupStatusBar()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, Observer { render(it!!) })
      messageLiveData.observe(viewLifecycleOwner, Observer { showSnack(it) })
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
      translationY = mainActivity().discoverSearchViewPosition
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
      fadeIf(!mainActivity().isTipShown(DISCOVER_FILTERS))
      onClick {
        it.gone()
        mainActivity().showTip(DISCOVER_FILTERS)
      }
    }
  }

  private fun setupRecycler() {
    layoutManager = GridLayoutManager(context, gridSpan)
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
      setProgressBackgroundColorSchemeResource(R.color.colorSearchViewBackground)
      setColorSchemeResources(R.color.colorAccent, R.color.colorAccent, R.color.colorAccent)
      setProgressViewOffset(false, swipeRefreshStartOffset, swipeRefreshEndOffset)
      setOnRefreshListener {
        mainActivity().discoverSearchViewPosition = 0F
        viewModel.loadDiscoverShows(pullToRefresh = true)
      }
    }
  }

  private fun setupStatusBar() {
    discoverRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      discoverRecycler
        .updatePadding(top = statusBarSize + dimenToPx(R.dimen.discoverRecyclerPadding))
      (discoverSearchView.layoutParams as MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.spaceSmall))
      (discoverFiltersView.layoutParams as MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.searchViewHeight))
      discoverTipFilters.translationY = statusBarSize.toFloat()
    }
  }

  private fun navigateToSearch() {
    disableUi()
    saveUi()
    hideNavigation()
    discoverFiltersView.fadeOut()
    discoverRecycler.fadeOut(duration = 200) {
      enableUi()
      super.navigateTo(R.id.actionDiscoverFragmentToSearchFragment, null)
    }
  }

  private fun navigateToDetails(item: DiscoverListItem) {
    disableUi()
    saveUi()
    hideNavigation()
    animateItemsExit(item)
    discoverSearchView.fadeOut()
    discoverFiltersView.fadeOut()
  }

  private fun animateItemsExit(item: DiscoverListItem) {
    val clickedIndex = adapter.indexOf(item)
    (0..adapter.itemCount).forEach {
      if (it != clickedIndex) {
        val view = discoverRecycler.findViewHolderForAdapterPosition(it)
        view?.let { v ->
          val randomDelay = Random.nextLong(50, 200)
          v.itemView.fadeOut(duration = 150, startDelay = randomDelay)
        }
      }
    }
    val clickedView = discoverRecycler.findViewHolderForAdapterPosition(clickedIndex)
    clickedView?.itemView?.fadeOut(duration = 150, startDelay = 350, endAction = {
      enableUi()
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, item.show.ids.trakt.id) }
      navigateTo(R.id.actionDiscoverFragmentToShowDetailsFragment, bundle)
    })
  }

  private fun toggleFiltersView() {
    if (!discoverFiltersView.isVisible) {
      val cx = discoverFiltersView.width
      val cy = discoverFiltersView.height
      val radius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
      val anim = ViewAnimationUtils.createCircularReveal(discoverFiltersView, cx, 0, 0F, radius)
      discoverFiltersView.visible()
      discoverMask.fadeIn()
      anim.start()
    } else {
      val cx = discoverFiltersView.width
      val cy = discoverFiltersView.height
      val radius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
      ViewAnimationUtils.createCircularReveal(discoverFiltersView, cx, 0, radius, 0F).apply {
        doOnEnd { discoverFiltersView.invisible() }
        start()
      }
      discoverMask.fadeOut()
    }
  }

  private fun saveUi() {
    mainActivity().discoverSearchViewPosition = discoverSearchView.translationY
  }

  private fun render(uiModel: DiscoverUiModel) {
    uiModel.run {
      shows?.let {
        adapter.setItems(it, scrollToTop == true)
        layoutManager.withSpanSizeLookup { pos -> adapter.getItems()[pos].image.type.spanSize }
        discoverRecycler.fadeIn()
      }
      showLoading?.let {
        discoverSearchView.isClickable = !it
        discoverSearchView.isEnabled = !it
        discoverSwipeRefresh.isRefreshing = it
      }
      filters?.let {
        discoverFiltersView.run {
          if (!this.isVisible) bind(it)
        }
      }
    }
  }

  override fun onTabReselected() = navigateToSearch()
}
