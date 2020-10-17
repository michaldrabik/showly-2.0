package com.michaldrabik.ui_progress.calendar

import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.calendar.recycler.ProgressCalendarAdapter
import com.michaldrabik.ui_progress.di.UiProgressComponentProvider
import com.michaldrabik.ui_progress.main.ProgressFragment
import com.michaldrabik.ui_progress.main.ProgressViewModel
import kotlinx.android.synthetic.main.fragment_progress_calendar.*

class ProgressCalendarFragment :
  BaseFragment<ProgressCalendarViewModel>(R.layout.fragment_progress_calendar),
  OnScrollResetListener {

  private val parentViewModel by viewModels<ProgressViewModel>({ requireParentFragment() }) { viewModelFactory }
  override val viewModel by viewModels<ProgressCalendarViewModel> { viewModelFactory }

  private var statusBarHeight = 0
  private lateinit var adapter: ProgressCalendarAdapter
  private lateinit var layoutManager: LinearLayoutManager

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiProgressComponentProvider).provideProgressComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupRecycler()
    setupStatusBar()

    parentViewModel.uiLiveData.observe(viewLifecycleOwner, { viewModel.handleParentAction(it) })
    viewModel.uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
  }

  private fun setupRecycler() {
    adapter = ProgressCalendarAdapter()
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    progressCalendarRecycler.apply {
      adapter = this@ProgressCalendarFragment.adapter
      layoutManager = this@ProgressCalendarFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
    adapter.run {
      itemClickListener = { (requireParentFragment() as ProgressFragment).openShowDetails(it) }
      detailsClickListener = { (requireParentFragment() as ProgressFragment).openEpisodeDetails(it.show.ids.trakt, it.upcomingEpisode) }
      missingImageListener = { item, force -> viewModel.findMissingImage(item, force) }
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      progressCalendarRecycler.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.progressCalendarTabsViewPadding))
      return
    }
    progressCalendarRecycler.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.progressCalendarTabsViewPadding))
    }
  }

  override fun onScrollReset() = progressCalendarRecycler.smoothScrollToPosition(0)

  private fun render(uiModel: ProgressCalendarUiModel) {
    uiModel.run {
      items?.let {
        adapter.setItems(it)
        progressCalendarRecycler.fadeIn()
        progressCalendarEmptyView.visibleIf(it.isEmpty())
      }
    }
  }
}
