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
import com.michaldrabik.ui_progress.calendar.helpers.CalendarMode
import com.michaldrabik.ui_progress.calendar.recycler.CalendarAdapter
import com.michaldrabik.ui_progress.main.ProgressMainFragment
import com.michaldrabik.ui_progress.main.ProgressMainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_calendar.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CalendarFragment :
  BaseFragment<CalendarViewModel>(R.layout.fragment_calendar),
  OnScrollResetListener {

  override val viewModel by viewModels<CalendarViewModel>()
  private val parentViewModel by viewModels<ProgressMainViewModel>({ requireParentFragment() })

  private var adapter: CalendarAdapter? = null
  private var layoutManager: LinearLayoutManager? = null
  private var statusBarHeight = 0

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
          parentViewModel.setWatchedEpisode(requireAppContext(), bundle)
        }
      }
    }
    progressCalendarRecycler.apply {
      adapter = this@CalendarFragment.adapter
      layoutManager = this@CalendarFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
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
        parentViewModel.setWatchedEpisode(requireAppContext(), bundle)
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
        progressCalendarEmptyFutureView.visibleIf(items.isEmpty() && mode == CalendarMode.PRESENT_FUTURE)
        progressCalendarEmptyRecentsView.visibleIf(items.isEmpty() && mode == CalendarMode.RECENTS)
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
