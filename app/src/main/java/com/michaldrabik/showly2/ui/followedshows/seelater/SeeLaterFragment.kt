package com.michaldrabik.showly2.ui.followedshows.seelater

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.model.SortOrder.DATE_ADDED
import com.michaldrabik.showly2.model.SortOrder.NAME
import com.michaldrabik.showly2.model.SortOrder.NEWEST
import com.michaldrabik.showly2.model.SortOrder.RATING
import com.michaldrabik.showly2.ui.common.OnScrollResetListener
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.OnTraktSyncListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.followedshows.FollowedShowsFragment
import com.michaldrabik.showly2.ui.followedshows.seelater.recycler.SeeLaterAdapter
import com.michaldrabik.showly2.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.showly2.utilities.extensions.fadeIf
import com.michaldrabik.showly2.utilities.extensions.onClick
import kotlinx.android.synthetic.main.fragment_see_later.*

class SeeLaterFragment : BaseFragment<SeeLaterViewModel>(R.layout.fragment_see_later),
  OnTabReselectedListener,
  OnScrollResetListener,
  OnTraktSyncListener {

  override val viewModel by viewModels<SeeLaterViewModel> { viewModelFactory }

  private lateinit var adapter: SeeLaterAdapter
  private lateinit var layoutManager: LinearLayoutManager

  override fun onCreate(savedInstanceState: Bundle?) {
    fragmentComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupRecycler()
    setupStatusBar()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, Observer { render(it!!) })
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

  private fun setupStatusBar() {
    seeLaterContent.doOnApplyWindowInsets { view, insets, padding, _ ->
      view.updatePadding(top = padding.top + insets.systemWindowInsetTop)
    }
  }

  private fun showSortOrderDialog(order: SortOrder) {
    val options = listOf(NAME, RATING, NEWEST, DATE_ADDED)
    val optionsStrings = options.map { getString(it.displayString) }.toTypedArray()
    AlertDialog.Builder(requireContext(), R.style.ChoiceDialog)
      .setTitle(R.string.textSortBy)
      .setSingleChoiceItems(optionsStrings, options.indexOf(order)) { dialog, index ->
        viewModel.setSortOrder(options[index])
        dialog.dismiss()
      }
      .show()
  }

  private fun render(uiModel: SeeLaterUiModel) {
    uiModel.run {
      items?.let {
        adapter.setItems(it)
        seeLaterEmptyView.fadeIf(it.isEmpty())
      }
      sortOrder?.let { order ->
        seeLaterSortIcon.onClick { showSortOrderDialog(order) }
      }
    }
  }

  private fun openShowDetails(show: Show) {
    (parentFragment as? FollowedShowsFragment)?.openShowDetails(show)
  }

  fun onTabScrollPosition(position: Float) {
    seeLaterSortIcon.alpha = 1F - (2F * position)
  }

  override fun onTabReselected() = onScrollReset()

  override fun onScrollReset() = seeLaterRoot.smoothScrollTo(0, 0)

  override fun onTraktSyncProgress() = viewModel.loadShows()
}
