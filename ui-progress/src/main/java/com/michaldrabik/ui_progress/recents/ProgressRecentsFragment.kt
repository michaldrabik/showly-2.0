package com.michaldrabik.ui_progress.recents

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
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
import com.michaldrabik.ui_progress.main.ProgressFragment
import com.michaldrabik.ui_progress.main.ProgressViewModel
import com.michaldrabik.ui_progress.recents.recycler.ProgressRecentsAdapter
import com.michaldrabik.ui_progress.recents.recycler.RecentsListItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_progress_calendar.*
import kotlinx.android.synthetic.main.fragment_progress_recents.*

@AndroidEntryPoint
class ProgressRecentsFragment :
  BaseFragment<ProgressRecentsViewModel>(R.layout.fragment_progress_recents),
  OnScrollResetListener {

  override val viewModel by viewModels<ProgressRecentsViewModel>()
  private val parentViewModel by viewModels<ProgressViewModel>({ requireParentFragment() })

  private var adapter: ProgressRecentsAdapter? = null
  private var layoutManager: LinearLayoutManager? = null
  private var statusBarHeight = 0

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupRecycler()
    setupStatusBar()

    parentViewModel.uiLiveData.observe(viewLifecycleOwner, { viewModel.handleParentAction(it) })
    with(viewModel) {
      itemsLiveData.observe(viewLifecycleOwner, { render(it) })
      messageLiveData.observe(viewLifecycleOwner, { showSnack(it) })
      checkQuickRateEnabled()
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = ProgressRecentsAdapter().apply {
      itemClickListener = { (requireParentFragment() as ProgressFragment).openShowDetails(it.show) }
      detailsClickListener = { (requireParentFragment() as ProgressFragment).openEpisodeDetails(it.show, it.episode, it.season) }
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
    progressRecentsRecycler.apply {
      adapter = this@ProgressRecentsFragment.adapter
      layoutManager = this@ProgressRecentsFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
  }

  private fun setupStatusBar() {
    val recyclerPadding =
      if (moviesEnabled) R.dimen.progressCalendarTabsViewPadding
      else R.dimen.progressCalendarTabsViewPaddingNoModes

    if (statusBarHeight != 0) {
      progressRecentsRecycler.updatePadding(top = statusBarHeight + dimenToPx(recyclerPadding))
      return
    }

    progressRecentsRecycler.doOnApplyWindowInsets { view, insets, _, _ ->
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

  private fun render(items: List<RecentsListItem>) {
    adapter?.setItems(items)
    progressRecentsRecycler.fadeIn(150, withHardware = true)
    progressRecentsEmptyView.visibleIf(items.isEmpty())
  }

  override fun onScrollReset() = progressRecentsRecycler.smoothScrollToPosition(0)

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }

  override fun setupBackPressed() = Unit
}
