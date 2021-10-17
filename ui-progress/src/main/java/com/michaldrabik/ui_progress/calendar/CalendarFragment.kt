package com.michaldrabik.ui_progress.calendar

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.views.RateView
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.calendar.helpers.CalendarMode.PRESENT_FUTURE
import com.michaldrabik.ui_progress.calendar.helpers.CalendarMode.RECENTS
import com.michaldrabik.ui_progress.calendar.helpers.TopOverscrollAdapter
import com.michaldrabik.ui_progress.calendar.recycler.CalendarAdapter
import com.michaldrabik.ui_progress.main.ProgressMainFragment
import com.michaldrabik.ui_progress.main.ProgressMainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_calendar.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.everything.android.ui.overscroll.IOverScrollState
import me.everything.android.ui.overscroll.OverScrollBounceEffectDecoratorBase.DEFAULT_DECELERATE_FACTOR
import me.everything.android.ui.overscroll.OverScrollBounceEffectDecoratorBase.DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator

@AndroidEntryPoint
class CalendarFragment :
  BaseFragment<CalendarViewModel>(R.layout.fragment_calendar),
  OnScrollResetListener {

  private companion object {
    const val OVERSCROLL_OFFSET = 75F
    const val OVERSCROLL_OFFSET_TRANSLATION = 5F
  }

  override val viewModel by viewModels<CalendarViewModel>()
  private val parentViewModel by viewModels<ProgressMainViewModel>({ requireParentFragment() })

  private var adapter: CalendarAdapter? = null
  private var layoutManager: LinearLayoutManager? = null
  private var statusBarHeight = 0
  private var overscrollEnabled = true

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupRecycler()
    setupStatusBar()

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(parentViewModel) {
          launch { uiState.collect { viewModel.handleParentAction(it) } }
        }
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          launch { messageState.collect { showSnack(it) } }
          checkQuickRateEnabled()
        }
      }
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = CalendarAdapter().apply {
      itemClickListener = { (requireParentFragment() as ProgressMainFragment).openShowDetails(it.show) }
      detailsClickListener = { (requireParentFragment() as ProgressMainFragment).openEpisodeDetails(it.show, it.episode, it.season) }
      missingImageListener = { item, force -> viewModel.findMissingImage(item, force) }
      missingTranslationListener = { viewModel.findMissingTranslation(it) }
      checkClickListener = {
        val bundle = EpisodeBundle(it.episode, it.season, it.show)
        if (viewModel.isQuickRateEnabled) {
          openRateDialog(bundle)
        } else {
          parentViewModel.setWatchedEpisode(bundle)
        }
      }
    }
    progressCalendarRecycler.apply {
      adapter = this@CalendarFragment.adapter
      layoutManager = this@CalendarFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
    setupOverscroll()
  }

  private fun setupOverscroll() {
    val adapt = TopOverscrollAdapter(progressCalendarRecycler)
    val overscroll = VerticalOverScrollBounceEffectDecorator(
      adapt,
      1.75F,
      DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK,
      DEFAULT_DECELERATE_FACTOR
    )
    overscroll.setOverScrollUpdateListener { _, state, offset ->
      with(progressCalendarOverscrollIcon) {
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
                (requireParentFragment() as ProgressMainFragment).toggleCalendarMode()
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

  private fun setupStatusBar() {
    val recyclerPadding =
      if (moviesEnabled) R.dimen.progressCalendarTabsViewPadding
      else R.dimen.progressCalendarTabsViewPaddingNoModes

    if (statusBarHeight != 0) {
      progressCalendarRecycler.updatePadding(top = statusBarHeight + dimenToPx(recyclerPadding))
      return
    }

    progressCalendarRecycler.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = statusBarHeight + dimenToPx(recyclerPadding))
    }
  }

  private fun openRateDialog(bundle: EpisodeBundle) {
    val context = requireContext()
    val rateView = RateView(context).apply {
      setPadding(context.dimenToPx(R.dimen.spaceNormal))
      setRating(5)
    }
    MaterialAlertDialogBuilder(context, R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(context, R.drawable.bg_dialog))
      .setView(rateView)
      .setPositiveButton(R.string.textRate) { _, _ ->
        parentViewModel.setWatchedEpisode(bundle)
        viewModel.addRating(rateView.getRating(), bundle)
      }
      .setNegativeButton(R.string.textCancel) { _, _ -> }
      .show()
  }

  private fun render(uiState: CalendarUiState) {
    uiState.run {
      items?.let {
        adapter?.setItems(it)
        progressCalendarRecycler.fadeIn(150, withHardware = true)
        progressCalendarEmptyFutureView.visibleIf(items.isEmpty() && mode == PRESENT_FUTURE)
        progressCalendarEmptyRecentsView.visibleIf(items.isEmpty() && mode == RECENTS)
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

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }

  override fun setupBackPressed() = Unit
}
