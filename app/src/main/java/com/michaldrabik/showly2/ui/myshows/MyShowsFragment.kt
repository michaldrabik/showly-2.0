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
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.myshows.recycler.MyShowsHorizontalAdapter
import com.michaldrabik.showly2.ui.myshows.views.MyShowView
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment.Companion.ARG_SHOW_ID
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import kotlinx.android.synthetic.main.fragment_my_shows.*

class MyShowsFragment : BaseFragment<MyShowsViewModel>() {

  override val layoutResId = R.layout.fragment_my_shows

  private val runningShowsAdapter by lazy { MyShowsHorizontalAdapter() }
  private val endedShowsAdapter by lazy { MyShowsHorizontalAdapter() }

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
    setupRunningShowsList()
    setupEndedShowsList()
  }

  private fun setupRunningShowsList() {
    val context = requireContext()
    myShowsRunningRecycler.apply {
      setHasFixedSize(true)
      adapter = runningShowsAdapter
      layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
      addItemDecoration(DividerItemDecoration(context, HORIZONTAL).apply {
        setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_my_shows_horizontal)!!)
      })
    }
  }

  private fun setupEndedShowsList() {
    val context = requireContext()
    myShowsEndedRecycler.apply {
      setHasFixedSize(true)
      adapter = endedShowsAdapter
      layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
      addItemDecoration(DividerItemDecoration(context, HORIZONTAL).apply {
        setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_my_shows_horizontal)!!)
      })
    }
  }

  private fun render(uiModel: MyShowsUiModel) {
    uiModel.recentShows?.let { renderRecentlyAdded(it) }
    uiModel.runningShows?.let { runningShowsAdapter.setItems(it) }
    uiModel.endedShows?.let { endedShowsAdapter.setItems(it) }
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
    val bundle = Bundle().apply { putLong(ARG_SHOW_ID, show.id) }
    findNavController().navigate(R.id.actionMyShowsFragmentToShowDetailsFragment, bundle)
    getMainActivity().hideNavigation()
  }
}
