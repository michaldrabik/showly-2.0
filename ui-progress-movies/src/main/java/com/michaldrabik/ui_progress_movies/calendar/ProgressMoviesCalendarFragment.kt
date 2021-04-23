package com.michaldrabik.ui_progress_movies.calendar

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
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.calendar.recycler.ProgressMoviesCalendarAdapter
import com.michaldrabik.ui_progress_movies.di.UiProgressMoviesComponentProvider
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesFragment
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesViewModel
import kotlinx.android.synthetic.main.fragment_progress_movies_calendar.*

class ProgressMoviesCalendarFragment :
  BaseFragment<ProgressMoviesCalendarViewModel>(R.layout.fragment_progress_movies_calendar),
  OnScrollResetListener {

  private val parentViewModel by viewModels<ProgressMoviesViewModel>({ requireParentFragment() }) { viewModelFactory }
  override val viewModel by viewModels<ProgressMoviesCalendarViewModel> { viewModelFactory }

  private var adapter: ProgressMoviesCalendarAdapter? = null
  private var layoutManager: LinearLayoutManager? = null
  private var statusBarHeight = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireAppContext() as UiProgressMoviesComponentProvider).provideProgressMoviesComponent().inject(this)
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
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = ProgressMoviesCalendarAdapter().apply {
      itemClickListener = { (requireParentFragment() as ProgressMoviesFragment).openMovieDetails(it) }
      missingImageListener = { item, force -> viewModel.findMissingImage(item, force) }
      missingTranslationListener = { item -> viewModel.findMissingTranslation(item) }
    }
    progressMoviesCalendarRecycler.apply {
      adapter = this@ProgressMoviesCalendarFragment.adapter
      layoutManager = this@ProgressMoviesCalendarFragment.layoutManager
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

  private fun render(uiModel: ProgressMoviesCalendarUiModel) {
    uiModel.run {
      items?.let {
        adapter?.setItems(it)
        progressMoviesCalendarRecycler.fadeIn()
        progressMoviesCalendarEmptyView.visibleIf(it.isEmpty())
      }
    }
  }

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
