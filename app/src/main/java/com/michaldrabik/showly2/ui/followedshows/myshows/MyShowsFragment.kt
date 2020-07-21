package com.michaldrabik.showly2.ui.followedshows.myshows

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
import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.model.SortOrder.NAME
import com.michaldrabik.showly2.model.SortOrder.NEWEST
import com.michaldrabik.showly2.model.SortOrder.RATING
import com.michaldrabik.showly2.ui.common.OnScrollResetListener
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.OnTraktSyncListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.followedshows.FollowedShowsFragment
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsAdapter
import com.michaldrabik.showly2.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.showly2.utilities.extensions.fadeIf
import kotlinx.android.synthetic.main.fragment_my_shows.*

class MyShowsFragment : BaseFragment<MyShowsViewModel>(R.layout.fragment_my_shows),
  OnTabReselectedListener,
  OnScrollResetListener,
  OnTraktSyncListener {

  override val viewModel by viewModels<MyShowsViewModel> { viewModelFactory }

  private val adapter by lazy { MyShowsAdapter() }
  private lateinit var layoutManager: LinearLayoutManager
  private var statusBarHeight = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    fragmentComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupStatusBar()
    setupRecycler()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, Observer { render(it!!) })
      loadShows()
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    myShowsRecycler.apply {
      adapter = this@MyShowsFragment.adapter
      layoutManager = this@MyShowsFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
    adapter.run {
      itemClickListener = { openShowDetails(it.show) }
      missingImageListener = { item, force ->
        viewModel.loadMissingImage(item, force)
      }
      sectionMissingImageListener = { item, section, force ->
        viewModel.loadSectionMissingItem(item, section, force)
      }
      onSortOrderClickListener = { section, order ->
        showSortOrderDialog(section, order)
      }
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      myShowsRoot.updatePadding(top = statusBarHeight)
      return
    }
    myShowsRoot.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = statusBarHeight)
    }
  }

  private fun showSortOrderDialog(section: MyShowsSection, order: SortOrder) {
    val options = listOf(NAME, RATING, NEWEST)
    val optionsStrings = options.map { getString(it.displayString) }.toTypedArray()
    AlertDialog.Builder(requireContext(), R.style.ChoiceDialog)
      .setTitle(R.string.textSortBy)
      .setSingleChoiceItems(optionsStrings, options.indexOf(order)) { dialog, index ->
        viewModel.loadSortedSection(section, options[index])
        dialog.dismiss()
      }
      .show()
  }

  private fun render(uiModel: MyShowsUiModel) {
    uiModel.run {
      listItems?.let {
        adapter.notifyListsUpdate = notifyListsUpdate ?: false
        adapter.setItems(it)
        myShowsEmptyView.fadeIf(it.isEmpty())
        (parentFragment as FollowedShowsFragment).enableSearch(it.isNotEmpty())
      }
    }
  }

  private fun openShowDetails(show: Show) {
    (parentFragment as? FollowedShowsFragment)?.openShowDetails(show)
  }

  override fun onTabReselected() = onScrollReset()

  override fun onScrollReset() = myShowsRecycler.scrollToPosition(0)

  override fun onTraktSyncProgress() = viewModel.loadShows()
}
