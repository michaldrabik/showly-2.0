package com.michaldrabik.showly2.ui.followedshows.seelater

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.SortOrder.DATE_ADDED
import com.michaldrabik.showly2.model.SortOrder.NAME
import com.michaldrabik.showly2.model.SortOrder.NEWEST
import com.michaldrabik.showly2.model.SortOrder.RATING
import com.michaldrabik.showly2.ui.common.OnScrollResetListener
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.followedshows.FollowedShowsFragment
import com.michaldrabik.showly2.ui.followedshows.seelater.recycler.SeeLaterAdapter
import com.michaldrabik.showly2.utilities.extensions.fadeIf
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.onClick
import kotlinx.android.synthetic.main.fragment_see_later.*

class SeeLaterFragment : BaseFragment<SeeLaterViewModel>(), OnTabReselectedListener, OnScrollResetListener {

  override val layoutResId = R.layout.fragment_see_later

  private lateinit var adapter: SeeLaterAdapter
  private lateinit var layoutManager: LinearLayoutManager

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun createViewModel(provider: ViewModelProvider) =
    provider.get(SeeLaterViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()
    viewModel.run {
      uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      loadShows()
    }
  }

  private fun setupView() {
    seeLaterSortIcon.onClick { seeLaterSortView.fadeIn() }
    seeLaterSortView.run {
      setAvailable(listOf(NAME, DATE_ADDED, RATING, NEWEST))
      sortSelectedListener = {
        fadeOut()
        viewModel.setSortOrder(it)
      }
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(requireContext(), VERTICAL, false)
    adapter = SeeLaterAdapter()
    adapter.missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
    adapter.itemClickListener = { openShowDetails(it.show) }
    seeLaterRecycler.apply {
      setHasFixedSize(true)
      adapter = this@SeeLaterFragment.adapter
      layoutManager = this@SeeLaterFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }
  }

  private fun render(uiModel: SeeLaterUiModel) {
    uiModel.run {
      items?.let {
        adapter.setItems(it)
        seeLaterEmptyView.fadeIf(it.isEmpty())
      }
      sortOrder?.let { seeLaterSortView.bind(it) }
    }
  }

  private fun openShowDetails(show: Show) {
    (parentFragment as? FollowedShowsFragment)?.openShowDetails(show)
  }

  override fun onTabReselected() = onScrollReset()

  override fun onScrollReset() = seeLaterRecycler.scrollToPosition(0)
}
