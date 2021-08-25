package com.michaldrabik.ui_progress_movies.calendar

import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.calendar.helpers.CalendarMode
import com.michaldrabik.ui_progress_movies.calendar.recycler.CalendarMoviesAdapter
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesMainFragment
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesMainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_calendar_movies.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CalendarMoviesFragment :
  BaseFragment<CalendarMoviesViewModel>(R.layout.fragment_calendar_movies),
  OnScrollResetListener {

  private val parentViewModel by viewModels<ProgressMoviesMainViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<CalendarMoviesViewModel>()

  private var adapter: CalendarMoviesAdapter? = null
  private var layoutManager: LinearLayoutManager? = null
  private var statusBarHeight = 0

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupRecycler()
    setupStatusBar()

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(parentViewModel) {
          launch { uiState.collect { viewModel.onParentState(it) } }
        }
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          checkQuickRateEnabled()
        }
      }
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = CalendarMoviesAdapter(
      itemClickListener = { (requireParentFragment() as ProgressMoviesMainFragment).openMovieDetails(it.movie) },
      missingImageListener = { item, force -> viewModel.findMissingImage(item, force) },
      missingTranslationListener = { item -> viewModel.findMissingTranslation(item) }
    )
    progressMoviesCalendarRecycler.apply {
      adapter = this@CalendarMoviesFragment.adapter
      layoutManager = this@CalendarMoviesFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      progressMoviesCalendarRecycler.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.progressMoviesCalendarTabsViewPadding))
      return
    }
    progressMoviesCalendarRecycler.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.progressMoviesCalendarTabsViewPadding))
    }
  }

  override fun onScrollReset() = progressMoviesCalendarRecycler.smoothScrollToPosition(0)

  private fun render(uiState: CalendarMoviesUiState) {
    uiState.run {
      items?.let {
        adapter?.setItems(it)
        progressMoviesCalendarRecycler.fadeIn(150, withHardware = true)
        progressMoviesCalendarEmptyFutureView.visibleIf(items.isEmpty() && mode == CalendarMode.PRESENT_FUTURE)
        progressMoviesCalendarEmptyRecentsView.visibleIf(items.isEmpty() && mode == CalendarMode.RECENTS)
      }
    }
  }

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
