package com.michaldrabik.ui_progress_movies.calendar

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateMargins
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
import com.michaldrabik.ui_base.common.OnSearchClickListener
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.CalendarMode.PRESENT_FUTURE
import com.michaldrabik.ui_model.CalendarMode.RECENTS
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.calendar.recycler.CalendarMoviesAdapter
import com.michaldrabik.ui_progress_movies.helpers.TopOverscrollAdapter
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesMainFragment
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesMainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_calendar_movies.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.everything.android.ui.overscroll.IOverScrollDecor
import me.everything.android.ui.overscroll.IOverScrollState
import me.everything.android.ui.overscroll.OverScrollBounceEffectDecoratorBase.DEFAULT_DECELERATE_FACTOR
import me.everything.android.ui.overscroll.OverScrollBounceEffectDecoratorBase.DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator

@AndroidEntryPoint
class CalendarMoviesFragment :
  BaseFragment<CalendarMoviesViewModel>(R.layout.fragment_calendar_movies),
  OnSearchClickListener,
  OnScrollResetListener {

  private companion object {
    const val OVERSCROLL_OFFSET = 100F
    const val OVERSCROLL_OFFSET_TRANSLATION = 5F
  }

  private val parentViewModel by viewModels<ProgressMoviesMainViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<CalendarMoviesViewModel>()

  private var adapter: CalendarMoviesAdapter? = null
  private var layoutManager: LinearLayoutManager? = null
  private var overscroll: IOverScrollDecor? = null
  private var overscrollEnabled = true
  private var statusBarHeight = 0
  private var isSearching = false

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupRecycler()
    setupStatusBar()

    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
      itemClickListener = { requireMainFragment().openMovieDetails(it.movie) },
      itemLongClickListener = { requireMainFragment().openMovieMenu(it.movie, showPinButtons = false) },
      missingImageListener = { item, force -> viewModel.findMissingImage(item, force) },
      missingTranslationListener = { item -> viewModel.findMissingTranslation(item) }
    )
    progressMoviesCalendarRecycler.apply {
      adapter = this@CalendarMoviesFragment.adapter
      layoutManager = this@CalendarMoviesFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
    setupOverscroll()
  }

  private fun setupOverscroll() {
    if (overscroll != null) return
    val adapt = TopOverscrollAdapter(progressMoviesCalendarRecycler)
    overscroll = VerticalOverScrollBounceEffectDecorator(
      adapt,
      1.75F,
      DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK,
      DEFAULT_DECELERATE_FACTOR
    ).apply {
      setOverScrollUpdateListener { _, state, offset ->
        progressMoviesCalendarOverscrollIcon?.run {
          if (offset > 0) {
            val value = (offset / OVERSCROLL_OFFSET).coerceAtMost(1F)
            val valueTranslation = offset / OVERSCROLL_OFFSET_TRANSLATION
            when (state) {
              IOverScrollState.STATE_DRAG_START_SIDE -> {
                alpha = value
                scaleX = value
                scaleY = value
                translationY = valueTranslation
                overscrollEnabled = true
              }
              IOverScrollState.STATE_BOUNCE_BACK -> {
                alpha = value
                scaleX = value
                scaleY = value
                translationY = valueTranslation
                if (offset >= OVERSCROLL_OFFSET && overscrollEnabled) {
                  overscrollEnabled = false
                  requireMainFragment().toggleCalendarMode()
                }
              }
            }
          } else {
            alpha = 0F
            scaleX = 0F
            scaleY = 0F
            translationY = 0F
          }
        }
      }
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      progressMoviesCalendarRecycler.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.progressMoviesCalendarTabsViewPadding))
      return
    }
    progressMoviesCalendarRecycler.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      view.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.progressMoviesCalendarTabsViewPadding))
      (progressMoviesCalendarOverscrollIcon.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarHeight + dimenToPx(R.dimen.progressMoviesOverscrollIconPadding))
    }
  }

  override fun onScrollReset() = progressMoviesCalendarRecycler.smoothScrollToPosition(0)

  override fun onEnterSearch() {
    isSearching = true

    progressMoviesCalendarRecycler.translationY = dimenToPx(R.dimen.progressMoviesSearchLocalOffset).toFloat()
    progressMoviesCalendarRecycler.smoothScrollToPosition(0)

    overscroll?.detach()
    overscroll = null
  }

  override fun onExitSearch() {
    isSearching = false

    progressMoviesCalendarRecycler.translationY = 0F
    progressMoviesCalendarRecycler.smoothScrollToPosition(0)

    setupOverscroll()
  }

  private fun render(uiState: CalendarMoviesUiState) {
    uiState.run {
      items?.let {
        adapter?.setItems(it)
        progressMoviesCalendarRecycler.fadeIn(150, withHardware = true)
        progressMoviesCalendarEmptyFutureView.visibleIf(items.isEmpty() && mode == PRESENT_FUTURE && !isSearching)
        progressMoviesCalendarEmptyRecentsView.visibleIf(items.isEmpty() && mode == RECENTS && !isSearching)
      }
      mode.let {
        viewLifecycleOwner.lifecycleScope.launch {
          delay(300)
          when (it) {
            PRESENT_FUTURE -> progressMoviesCalendarOverscrollIcon.setImageResource(R.drawable.ic_history)
            RECENTS -> progressMoviesCalendarOverscrollIcon.setImageResource(R.drawable.ic_calendar)
          }
        }
      }
    }
  }

  private fun requireMainFragment() = (requireParentFragment() as ProgressMoviesMainFragment)

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    overscroll = null
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
