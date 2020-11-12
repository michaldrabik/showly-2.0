package com.michaldrabik.ui_my_shows.seelater

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.common.OnTranslationsSyncListener
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.di.UiMyShowsComponentProvider
import com.michaldrabik.ui_my_shows.main.FollowedShowsFragment
import com.michaldrabik.ui_my_shows.main.utilities.OnSortClickListener
import com.michaldrabik.ui_my_shows.seelater.recycler.SeeLaterAdapter
import kotlinx.android.synthetic.main.fragment_see_later.*

class SeeLaterFragment :
  BaseFragment<SeeLaterViewModel>(R.layout.fragment_see_later),
  OnScrollResetListener,
  OnTraktSyncListener,
  OnTranslationsSyncListener,
  OnSortClickListener {

  override val viewModel by viewModels<SeeLaterViewModel> { viewModelFactory }

  private lateinit var adapter: SeeLaterAdapter
  private lateinit var layoutManager: LinearLayoutManager
  private var statusBarHeight = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiMyShowsComponentProvider).provideMyShowsComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupStatusBar()
    setupRecycler()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
      loadShows()
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(requireContext(), VERTICAL, false)
    adapter = SeeLaterAdapter().apply {
      missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
      itemClickListener = { openShowDetails(it.show) }
      listChangeListener = { seeLaterRecycler.scrollToPosition(0) }
    }
    seeLaterRecycler.apply {
      setHasFixedSize(true)
      adapter = this@SeeLaterFragment.adapter
      layoutManager = this@SeeLaterFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      seeLaterContent.updatePadding(top = seeLaterContent.paddingTop + statusBarHeight)
      return
    }
    seeLaterContent.doOnApplyWindowInsets { view, insets, padding, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = padding.top + statusBarHeight)
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

  private fun render(uiModel: SeeLaterUiModel) {
    uiModel.run {
      items?.let {
        val notifyChange = scrollToTop?.consume() == true
        adapter.setItems(it, notifyChange = notifyChange)
        seeLaterEmptyView.fadeIf(it.isEmpty())
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

  override fun onScrollReset() {
    seeLaterRecycler.scrollToPosition(0)
  }

  override fun onTraktSyncProgress() = viewModel.loadShows()

  override fun onTranslationsSyncProgress() = viewModel.loadShows()
}
