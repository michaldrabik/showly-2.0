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
import com.michaldrabik.showly2.ui.common.OnScrollResetListener
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.followedshows.FollowedShowsFragment
import com.michaldrabik.showly2.ui.followedshows.seelater.recycler.SeeLaterAdapter
import com.michaldrabik.showly2.utilities.extensions.fadeIf
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
    setupRecycler()
    viewModel.run {
      uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      loadShows()
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
    }
  }

  private fun openShowDetails(show: Show) {
    (parentFragment as? FollowedShowsFragment)?.openShowDetails(show)
  }

  override fun onTabReselected() = onScrollReset()

  override fun onScrollReset() = seeLaterRecycler.scrollToPosition(0)
}
