package com.michaldrabik.showly2.ui.discover

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.ui.UiCache
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverAdapter
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment.Companion.ARG_SHOW_ID
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.disableUi
import com.michaldrabik.showly2.utilities.extensions.enableUi
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.withSpanSizeLookup
import kotlinx.android.synthetic.main.fragment_discover.*
import kotlin.random.Random

class DiscoverFragment : BaseFragment<DiscoverViewModel>(), OnTabReselectedListener {

  override val layoutResId = R.layout.fragment_discover
  override val viewModel by viewModels<DiscoverViewModel> { viewModelFactory }

  private val gridSpan by lazy { resources.getInteger(R.integer.discoverGridSpan) }
  private val swipeRefreshStartOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshStartOffset) }
  private val swipeRefreshEndOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshEndOffset) }

  private lateinit var adapter: DiscoverAdapter
  private lateinit var layoutManager: GridLayoutManager

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()
    setupSwipeRefresh()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, Observer { render(it!!) })
      cacheStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      errorLiveData.observe(viewLifecycleOwner, Observer { showErrorSnackbar(it!!) })
      loadDiscoverShows()
    }
  }

  private fun setupView() {
    discoverSearchView.run {
      isClickable = false
      onClick { navigateTo(R.id.actionDiscoverFragmentToSearchFragment) }
      onSettingsClickListener = { navigateTo(R.id.actionDiscoverFragmentToSettingsFragment) }
    }
  }

  private fun setupRecycler() {
    layoutManager = GridLayoutManager(context, gridSpan)
    adapter = DiscoverAdapter()
    adapter.missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
    adapter.itemClickListener = { navigateToDetails(it) }
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
        viewModel.clearUiCache()
        viewModel.loadDiscoverShows(pullToRefresh = true)
      }
    }
  }

  private fun navigateTo(@IdRes id: Int) {
    disableUi()
    hideNavigation()
    saveUiPositions()
    discoverRecycler.fadeOut(duration = 200) {
      enableUi()
      super.navigateTo(id, null)
    }
  }

  private fun navigateToDetails(item: DiscoverListItem) {
    disableUi()
    saveUiPositions()
    hideNavigation()
    animateItemsExit(item)
    discoverSearchView.fadeOut()
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

  private fun saveUiPositions() {
    viewModel.saveUiPositions(discoverSearchView.translationY)
  }

  private fun render(uiModel: DiscoverUiModel) {
    uiModel.run {
      shows?.let {
        adapter.setItems(it)
        layoutManager.withSpanSizeLookup { pos -> adapter.getItems()[pos].image.type.spanSize }
        discoverRecycler.fadeIn()
      }
      showLoading?.let {
        discoverSearchView.isClickable = !it
        discoverSearchView.isEnabled = !it
        discoverSwipeRefresh.isRefreshing = it
      }
    }
  }

  private fun render(uiCache: UiCache) {
    uiCache.let {
      discoverSearchView.translationY = it.discoverSearchPosition
    }
  }

  override fun onTabReselected() = navigateTo(R.id.actionDiscoverFragmentToSearchFragment)
}
