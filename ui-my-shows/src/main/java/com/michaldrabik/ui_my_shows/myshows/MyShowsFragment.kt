package com.michaldrabik.ui_my_shows.myshows

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.main.FollowedShowsFragment
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_my_shows.*
import kotlinx.android.synthetic.main.fragment_watchlist.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyShowsFragment :
  BaseFragment<MyShowsViewModel>(R.layout.fragment_my_shows),
  OnScrollResetListener,
  OnTraktSyncListener {

  override val viewModel by viewModels<MyShowsViewModel>()

  private var adapter: MyShowsAdapter? = null
  private var layoutManager: LinearLayoutManager? = null
  private var horizontalPositions: MutableMap<MyShowsSection, Pair<Int, Int>>? = null
  private var statusBarHeight = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    savedInstanceState?.let { bundle ->
      val horizontalPositionsMap = mutableMapOf<MyShowsSection, Pair<Int, Int>>()
      MyShowsSection.values().forEach { section ->
        if (bundle.containsKey(section.name)) {
          @Suppress("UNCHECKED_CAST")
          horizontalPositionsMap[section] = bundle.getSerializable(section.name) as Pair<Int, Int>
        }
      }
      horizontalPositions = horizontalPositionsMap
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupStatusBar()
    setupRecycler()

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          loadShows()
        }
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    horizontalPositions?.entries?.forEach { (section, position) ->
      outState.putSerializable(section.name, position)
    }
  }

  override fun onPause() {
    horizontalPositions = adapter?.horizontalPositions?.toMutableMap()
    super.onPause()
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = MyShowsAdapter().apply {
      stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
      horizontalPositions = this@MyShowsFragment.horizontalPositions?.toMutableMap() ?: mutableMapOf()
      itemClickListener = { openShowDetails(it.show) }
      missingImageListener = { item, force -> viewModel.loadMissingImage(item, force) }
      missingTranslationListener = { viewModel.loadMissingTranslation(it) }
      sectionMissingImageListener = { item, section, force -> viewModel.loadSectionMissingItem(item, section, force) }
      onSortOrderClickListener = { section, order -> showSortOrderDialog(section, order) }
    }
    myShowsRecycler.apply {
      adapter = this@MyShowsFragment.adapter
      layoutManager = this@MyShowsFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      myShowsRoot.updatePadding(top = statusBarHeight)
      myShowsRecycler.updatePadding(top = dimenToPx(R.dimen.myShowsTabsViewPadding))
      return
    }
    myShowsRoot.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = statusBarHeight)
      myShowsRecycler.updatePadding(top = dimenToPx(R.dimen.myShowsTabsViewPadding))
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

  private fun render(uiState: MyShowsUiState) {
    uiState.run {
      items?.let {
        val notifyUpdate = notifyListsUpdate?.consume() == true
        adapter?.notifyListsUpdate = notifyUpdate
        adapter?.setItems(it)
        myShowsEmptyView.fadeIf(it.isEmpty())
        (parentFragment as FollowedShowsFragment).enableSearch(it.isNotEmpty())
      }
    }
  }

  private fun openShowDetails(show: Show) {
    (parentFragment as? FollowedShowsFragment)?.openShowDetails(show)
  }

  override fun onScrollReset() = myShowsRecycler.scrollToPosition(0)

  override fun onTraktSyncComplete() = viewModel.loadShows()

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
