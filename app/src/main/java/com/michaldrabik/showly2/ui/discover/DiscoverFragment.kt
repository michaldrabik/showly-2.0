package com.michaldrabik.showly2.ui.discover

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.ui.MainActivity
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverAdapter
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.showErrorSnackbar
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import com.michaldrabik.showly2.utilities.extensions.withSpanSizeLookup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_discover.*
import kotlin.random.Random

class DiscoverFragment : BaseFragment<DiscoverViewModel>() {

  override val layoutResId = R.layout.fragment_discover

  private val gridSpan by lazy { resources.getInteger(R.integer.discoverGridSpan) }
  private val searchViewPadding by lazy { resources.getDimensionPixelSize(R.dimen.searchViewHeightPadded) }
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
  }

  private fun openShowDetails(item: DiscoverListItem) {
    animateItemsExit(item)
    discoverSearchView.fadeOut()
    (activity as MainActivity).hideNavigation()
  }

  private fun openSearchView() {
    (activity as MainActivity).hideNavigation()
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
      val bundle = Bundle().apply { putLong("id", item.show.ids.trakt) }
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
      discoverProgress.visibleIf(it)
    }
    uiModel.updateListItem?.let { adapter.updateItem(it) }
    uiModel.listPosition?.let { layoutManager.scrollToPositionWithOffset(it.first, it.second) }
    uiModel.error?.let {
      requireActivity().snackBarHost.showErrorSnackbar(it.message ?: getString(R.string.errorGeneral))
    }
  }
}
