package com.michaldrabik.showly2.ui.discover

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverAdapter
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment.Companion.ARG_SHOW_ID
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.showErrorSnackbar
import com.michaldrabik.showly2.utilities.extensions.withSpanSizeLookup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_discover.*
import kotlin.random.Random

class DiscoverFragment : BaseFragment<DiscoverViewModel>(), OnTabReselectedListener {

  override val layoutResId = R.layout.fragment_discover

  private val gridSpan by lazy { resources.getInteger(R.integer.discoverGridSpan) }
  private val swipeRefreshStartOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshStartOffset) }
  private val swipeRefreshEndOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshEndOffset) }

  private lateinit var adapter: DiscoverAdapter
  private lateinit var layoutManager: GridLayoutManager

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(DiscoverViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()
    setupSwipeRefresh()

    viewModel.run {
      uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      discoverShowsStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      loadDiscoverShows()
    }
  }

  private fun setupView() {
    discoverSearchView.isClickable = false
    discoverSearchView.onClick { openSearchView() }
    discoverChipsView.onChipsSelectedListener = {
      saveUiPositions()
      viewModel.loadDiscoverShows(resetScroll = it.isEmpty())
    }
  }

  private fun setupRecycler() {
    layoutManager = GridLayoutManager(context, gridSpan)
    adapter = DiscoverAdapter()
    adapter.missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
    adapter.itemClickListener = { openShowDetails(it) }
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
        viewModel.clearCache()
        viewModel.loadDiscoverShows(skipCache = true, manual = true)
      }
    }
  }

  private fun openShowDetails(item: DiscoverListItem) {
    animateItemsExit(item)
    discoverChipsView.fadeOut()
    discoverSearchView.fadeOut()
    getMainActivity().hideNavigation()
  }

  private fun openSearchView() {
    saveUiPositions()
    getMainActivity().hideNavigation()
    discoverChipsView.fadeOut(200)
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
      saveUiPositions()
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, item.show.id) }
      findNavController().navigate(R.id.actionDiscoverFragmentToShowDetailsFragment, bundle)
    })
  }

  private fun saveUiPositions() {
    viewModel.saveUiPositions(
      discoverSearchView.translationY,
      discoverChipsView.translationY,
      discoverChipsView.selectedChips
    )
  }

  private fun render(items: List<DiscoverListItem>) {
    adapter.setItems(items)
    layoutManager.withSpanSizeLookup { pos -> items[pos].image.type.spanSize }
    discoverChipsView.fadeIn()
    discoverRecycler.fadeIn()
  }

  private fun render(uiModel: DiscoverUiModel) {
    uiModel.run {
      showLoading?.let {
        if (it) discoverChipsView.clear()
        discoverChipsView.isEnabled = !it
        discoverSearchView.isClickable = !it
        discoverSearchView.isEnabled = !it
        discoverSwipeRefresh.isRefreshing = it
      }
      updateListItem?.let { adapter.updateItem(it) }
      applyUiCache?.let {
        discoverSearchView.translationY = it.discoverSearchPosition
        discoverChipsView.translationY = it.discoverChipsPosition
        discoverChipsView.selectedChips = it.discoverActiveGenres
      }
      resetScroll?.let { if (it) discoverRecycler.scrollToPosition(0) }
      error?.let {
        requireActivity().snackBarHost.showErrorSnackbar(it.message ?: getString(R.string.errorGeneral))
      }
    }
  }

  override fun onTabReselected() = openSearchView()
}
