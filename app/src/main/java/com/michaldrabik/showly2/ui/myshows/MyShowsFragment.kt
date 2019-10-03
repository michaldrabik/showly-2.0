package com.michaldrabik.showly2.ui.myshows

import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.GridLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.myshows.recycler.MyShowsHorizontalAdapter
import com.michaldrabik.showly2.ui.myshows.views.MyShowView
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment.Companion.ARG_SHOW_ID
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.fadeIf
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.fragment_my_shows.*

class MyShowsFragment : BaseFragment<MyShowsViewModel>() {

  override val layoutResId = R.layout.fragment_my_shows

  private val runningShowsAdapter by lazy { MyShowsHorizontalAdapter() }
  private val endedShowsAdapter by lazy { MyShowsHorizontalAdapter() }
  private val incomingShowsAdapter by lazy { MyShowsHorizontalAdapter() }

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(MyShowsViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    viewModel.uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
    viewModel.loadMyShows()
  }

  private fun setupView() {
    setupRecycler(myShowsIncomingRecycler, incomingShowsAdapter)
    setupRecycler(myShowsRunningRecycler, runningShowsAdapter)
    setupRecycler(myShowsEndedRecycler, endedShowsAdapter)
  }

  private fun setupRecycler(
    recycler: RecyclerView,
    itemsAdapter: MyShowsHorizontalAdapter
  ) {
    val context = requireContext()
    recycler.apply {
      setHasFixedSize(true)
      adapter = itemsAdapter
      layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
      addItemDecoration(DividerItemDecoration(context, HORIZONTAL).apply {
        setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_my_shows_horizontal)!!)
      })
    }
    itemsAdapter.itemClickListener = { openShowDetails(it.show) }
  }

  private fun render(uiModel: MyShowsUiModel) {
    uiModel.recentShows?.let {
      renderRecentlyAdded(it)
      myShowsRootContent.fadeIf(it.isNotEmpty())
    }
    uiModel.incomingShows?.let {
      incomingShowsAdapter.clearItems()
      incomingShowsAdapter.setItems(it)
      myShowsIncomingGroup.visibleIf(it.isNotEmpty())
    }
    uiModel.runningShows?.let {
      runningShowsAdapter.clearItems()
      runningShowsAdapter.setItems(it)
      myShowsRunningGroup.visibleIf(it.isNotEmpty())
    }
    uiModel.endedShows?.let {
      endedShowsAdapter.clearItems()
      endedShowsAdapter.setItems(it)
      myShowsEndedGroup.visibleIf(it.isNotEmpty())
    }
  }

  private fun renderRecentlyAdded(items: List<MyShowListItem>) {
    myShowsRecentsContainer.removeAllViews()

    val context = requireContext()
    val itemHeight = context.dimenToPx(R.dimen.myShowsFanartHeight)
    val itemMargin = context.dimenToPx(R.dimen.spaceTiny)

    items.forEachIndexed { index, item ->
      val view = MyShowView(context).apply {
        layoutParams = FrameLayout.LayoutParams(0, MATCH_PARENT)
        bind(item.show, item.image)
        onItemClickListener = { openShowDetails(it) }
      }
      val layoutParams = GridLayout.LayoutParams().apply {
        width = 0
        height = itemHeight
        columnSpec = GridLayout.spec(index % 2, 1F)
        setMargins(itemMargin, itemMargin, itemMargin, itemMargin)
      }
      myShowsRecentsContainer.addView(view, layoutParams)
    }
  }

  private fun openShowDetails(show: Show) {
    //TODO Add fade transition
    myShowsRootContent.fadeOut {
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, show.id) }
      findNavController().navigate(R.id.actionMyShowsFragmentToShowDetailsFragment, bundle)
      getMainActivity().hideNavigation()
    }
  }
}
