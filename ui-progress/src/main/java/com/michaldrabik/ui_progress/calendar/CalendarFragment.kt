package com.michaldrabik.ui_progress.calendar

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
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.CalendarMode.PRESENT_FUTURE
import com.michaldrabik.ui_model.CalendarMode.RECENTS
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.calendar.recycler.CalendarAdapter
import com.michaldrabik.ui_progress.helpers.TopOverscrollAdapter
import com.michaldrabik.ui_progress.main.EpisodeCheckActionUiEvent
import com.michaldrabik.ui_progress.main.ProgressMainFragment
import com.michaldrabik.ui_progress.main.ProgressMainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_calendar.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.everything.android.ui.overscroll.IOverScrollDecor
import me.everything.android.ui.overscroll.IOverScrollState
import me.everything.android.ui.overscroll.OverScrollBounceEffectDecoratorBase.DEFAULT_DECELERATE_FACTOR
import me.everything.android.ui.overscroll.OverScrollBounceEffectDecoratorBase.DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator

@AndroidEntryPoint
class CalendarFragment :
  BaseFragment<CalendarViewModel>(R.layout.fragment_calendar),
  OnSearchClickListener,
  OnScrollResetListener {

  private companion object {
    const val OVERSCROLL_OFFSET = 100F
    const val OVERSCROLL_OFFSET_TRANSLATION = 5F
  }

  override val viewModel by viewModels<CalendarViewModel>()
  private val parentViewModel by viewModels<ProgressMainViewModel>({ requireParentFragment() })

  private var adapter: CalendarAdapter? = null
  private var layoutManager: LinearLayoutManager? = null
  private var overscroll: IOverScrollDecor? = null
  private var statusBarHeight = 0
  private var overscrollEnabled = true
  private var isSearching = false

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupRecycler()
    setupStatusBar()

    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(parentViewModel) {
          launch { uiState.collect { viewModel.handleParentAction(it) } }
        }
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          launch { messageFlow.collect { showSnack(it) } }
          launch { eventFlow.collect { handleEvent(it) } }
        }
      }
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = CalendarAdapter(
      itemClickListener = { requireMainFragment().openShowDetails(it.show) },
      missingImageListener = { item, force -> viewModel.findMissingImage(item, force) },
      checkClickListener = { viewModel.onEpisodeChecked(it) },
      detailsClickListener = { requireMainFragment().openEpisodeDetails(it.show, it.episode, it.season) },
      missingTranslationListener = { viewModel.findMissingTranslation(it) }
    )
    progressCalendarRecycler.apply {
      adapter = this@CalendarFragment.adapter
      layoutManager = this@CalendarFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
    setupOverscroll()
  }

  private fun setupOverscroll() {
    if (overscroll != null) return
    val adapt = TopOverscrollAdapter(progressCalendarRecycler)
    overscroll = VerticalOverScrollBounceEffectDecorator(
      adapt,
      1.75F,
      DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK,
      DEFAULT_DECELERATE_FACTOR
    ).apply {
      setOverScrollUpdateListener { _, state, offset ->
        progressCalendarOverscrollIcon?.run {
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
    val recyclerPadding = if (moviesEnabled) R.dimen.progressCalendarTabsViewPadding else R.dimen.progressCalendarTabsViewPaddingNoModes
    val overscrollPadding = if (moviesEnabled) R.dimen.progressOverscrollIconPadding else R.dimen.progressOverscrollIconPaddingNoModes

    if (statusBarHeight != 0) {
      progressCalendarRecycler.updatePadding(top = statusBarHeight + dimenToPx(recyclerPadding))
      (progressCalendarOverscrollIcon.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarHeight + dimenToPx(overscrollPadding))
      return
    }

    progressCalendarRecycler.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      view.updatePadding(top = statusBarHeight + dimenToPx(recyclerPadding))
      (progressCalendarOverscrollIcon.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarHeight + dimenToPx(overscrollPadding))
    }
  }

  override fun onEnterSearch() {
    isSearching = true

    progressCalendarRecycler.translationY = dimenToPx(R.dimen.progressSearchLocalOffset).toFloat()
    progressCalendarRecycler.smoothScrollToPosition(0)

    overscroll?.detach()
    overscroll = null
  }

  override fun onExitSearch() {
    isSearching = false

    progressCalendarRecycler.translationY = 0F
    progressCalendarRecycler.smoothScrollToPosition(0)

    setupOverscroll()
  }

  private fun handleEvent(event: Event<*>) {
    when (event) {
      is EpisodeCheckActionUiEvent -> {
        if (event.isQuickRate) requireMainFragment().openRateDialog(event.episode)
        else parentViewModel.setWatchedEpisode(event.episode)
      }
    }
  }

  private fun render(uiState: CalendarUiState) {
    uiState.run {
      items?.let {
        adapter?.setItems(it)
        progressCalendarRecycler.fadeIn(150, withHardware = true)
        progressCalendarEmptyFutureView.visibleIf(items.isEmpty() && mode == PRESENT_FUTURE && !isSearching)
        progressCalendarEmptyRecentsView.visibleIf(items.isEmpty() && mode == RECENTS && !isSearching)
      }
      mode.let {
        viewLifecycleOwner.lifecycleScope.launch {
          delay(300)
          when (it) {
            PRESENT_FUTURE -> progressCalendarOverscrollIcon.setImageResource(R.drawable.ic_history)
            RECENTS -> progressCalendarOverscrollIcon.setImageResource(R.drawable.ic_calendar)
          }
        }
      }
    }
  }

  override fun onScrollReset() = progressCalendarRecycler.smoothScrollToPosition(0)

  private fun requireMainFragment() = requireParentFragment() as ProgressMainFragment

  override fun onDestroyView() {
    overscroll?.detach()
    overscroll = null
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }

  override fun setupBackPressed() = Unit
}
