package com.michaldrabik.ui_my_shows.archive

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
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.archive.recycler.ArchiveAdapter
import com.michaldrabik.ui_my_shows.main.FollowedShowsFragment
import com.michaldrabik.ui_my_shows.main.utilities.OnSortClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_archive.*
import kotlinx.android.synthetic.main.fragment_watchlist.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArchiveFragment :
  BaseFragment<ArchiveViewModel>(R.layout.fragment_archive),
  OnScrollResetListener,
  OnTraktSyncListener,
  OnSortClickListener {

  override val viewModel by viewModels<ArchiveViewModel>()

  private var adapter: ArchiveAdapter? = null
  private var layoutManager: LinearLayoutManager? = null
  private var statusBarHeight = 0

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

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(requireContext(), VERTICAL, false)
    adapter = ArchiveAdapter().apply {
      stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
      missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
      missingTranslationListener = { viewModel.loadMissingTranslation(it) }
      itemClickListener = { openShowDetails(it.show) }
      listChangeListener = {
        archiveRecycler.scrollToPosition(0)
        (requireParentFragment() as FollowedShowsFragment).resetTranslations()
      }
    }
    archiveRecycler.apply {
      setHasFixedSize(true)
      adapter = this@ArchiveFragment.adapter
      layoutManager = this@ArchiveFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      archiveContent.updatePadding(top = archiveContent.paddingTop + statusBarHeight)
      archiveRecycler.updatePadding(top = dimenToPx(R.dimen.archiveTabsViewPadding))
      return
    }
    archiveContent.doOnApplyWindowInsets { view, insets, padding, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = padding.top + statusBarHeight)
      archiveRecycler.updatePadding(top = dimenToPx(R.dimen.archiveTabsViewPadding))
    }
  }

  private fun showSortOrderDialog(order: SortOrder) {
    val options = listOf(NAME, RATING, NEWEST, DATE_ADDED)
    val optionsStrings = options.map { getString(it.displayString) }.toTypedArray()

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setTitle(R.string.textSortBy)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(optionsStrings, options.indexOf(order)) { dialog, index ->
        viewModel.setSortOrder(options[index])
        dialog.dismiss()
      }
      .show()
  }

  private fun render(uiState: ArchiveUiState) {
    uiState.run {
      items.let {
        val notifyChange = resetScroll?.consume() == true
        adapter?.setItems(it, notifyChange = notifyChange)
        archiveEmptyView.fadeIf(it.isEmpty())
      }
      sortOrder?.let { event ->
        event.consume()?.let { showSortOrderDialog(it) }
      }
    }
  }

  private fun openShowDetails(show: Show) {
    (parentFragment as? FollowedShowsFragment)?.openShowDetails(show)
  }

  override fun onSortClick(page: Int) = viewModel.loadSortOrder()

  override fun onScrollReset() = archiveRecycler.scrollToPosition(0)

  override fun onTraktSyncComplete() = viewModel.loadShows()

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
