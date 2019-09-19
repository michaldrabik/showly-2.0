package com.michaldrabik.showly2.ui.discover

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverAdapter
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment.Companion.ARG_SHOW_ID
import com.michaldrabik.showly2.utilities.extensions.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_discover.*
import kotlin.random.Random

class DiscoverFragment : BaseFragment<DiscoverViewModel>() {

  override val layoutResId = R.layout.fragment_discover

  private val gridSpan by lazy { resources.getInteger(R.integer.discoverGridSpan) }
  private val searchViewPadding by lazy { requireContext().dimenToPx(R.dimen.searchViewHeightPadded) }
  private val swipeRefreshStartOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshStartOffset) }
  private val swipeRefreshEndOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshEndOffset) }

  private lateinit var adapter: DiscoverAdapter
  private lateinit var layoutManager: GridLayoutManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    appComponent().inject(this)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(DiscoverViewModel::class.java)
      .apply {
        uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    viewModel.loadTrendingShows()
  }

  private fun setupView() {
    discoverSearchView.isClickable = false
    discoverSearchView.onClick { openSearchView() }
    setupRecycler()
  }

  private fun setupRecycler() {
    layoutManager = GridLayoutManager(context, gridSpan)
    adapter = DiscoverAdapter()
    adapter.missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
    adapter.itemClickListener = { openShowDetails(it) }
    discoverRecycler.apply {
      setHasFixedSize(true)
      adapter = this@DiscoverFragment.adapter
      layoutManager = this@DiscoverFragment.layoutManager
    }

    discoverSwipeRefresh.apply {
      setProgressBackgroundColorSchemeResource(R.color.colorSearchViewBackground)
      setColorSchemeResources(R.color.colorAccent, R.color.colorAccent, R.color.colorAccent)
      setProgressViewOffset(false, swipeRefreshStartOffset, swipeRefreshEndOffset)
      setOnRefreshListener {
        adapter.clearItems()
        viewModel.saveListPosition(0, 0)
        viewModel.loadTrendingShows(skipCache = true)
      }
    }
  }

  private fun openShowDetails(item: DiscoverListItem) {
    animateItemsExit(item)
    discoverSearchView.fadeOut()
    getMainActivity().hideNavigation()
  }

  private fun openSearchView() {
    getMainActivity().hideNavigation()
    val position = layoutManager.findFirstVisibleItemPosition()
    viewModel.saveListPosition(position, (layoutManager.findViewByPosition(position)?.top ?: 0) - searchViewPadding)
    discoverRecycler.fadeOut(duration = 200) {
      findNavController().navigate(R.id.actionDiscoverFragmentToSearchFragment)
    }
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
      val position = layoutManager.findFirstVisibleItemPosition()
      viewModel.saveListPosition(position, (layoutManager.findViewByPosition(position)?.top ?: 0) - searchViewPadding)
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, item.show.ids.trakt) }
      findNavController().navigate(R.id.actionDiscoverFragmentToShowDetailsFragment, bundle)
    })
  }

  private fun render(uiModel: DiscoverUiModel) {
    uiModel.trendingShows?.let {
      adapter.setItems(it)
      layoutManager.withSpanSizeLookup { pos -> it[pos].image.type.spanSize }
    }
    uiModel.showLoading?.let {
      discoverSearchView.isClickable = !it
      discoverSwipeRefresh.isRefreshing = it
    }
    uiModel.updateListItem?.let { adapter.updateItem(it) }
    uiModel.listPosition?.let { layoutManager.scrollToPositionWithOffset(it.first, it.second) }
    uiModel.error?.let {
      requireActivity().snackBarHost.showErrorSnackbar(it.message ?: getString(R.string.errorGeneral))
    }
  }
}
