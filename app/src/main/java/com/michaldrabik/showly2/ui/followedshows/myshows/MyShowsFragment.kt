package com.michaldrabik.showly2.ui.followedshows.myshows

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.ui.common.OnScrollResetListener
import com.michaldrabik.showly2.ui.common.OnTraktSyncListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.followedshows.FollowedShowsFragment
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsAdapter
import com.michaldrabik.showly2.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.showly2.utilities.extensions.fadeIf
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import kotlinx.android.synthetic.main.fragment_my_shows.*

class MyShowsFragment : BaseFragment<MyShowsViewModel>(R.layout.fragment_my_shows),
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
    val options = listOf(NAME, RATING, NEWEST, DATE_ADDED)
    val optionsStrings = options.map { getString(it.displayString) }.toTypedArray()

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setTitle(R.string.textSortBy)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
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

  override fun onScrollReset() = myShowsRecycler.scrollToPosition(0)

  override fun onTraktSyncProgress() = viewModel.loadShows()
}
