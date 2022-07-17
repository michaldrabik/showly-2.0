package com.michaldrabik.ui_show.sections.seasons

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.WidgetsProvider
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.utilities.SnackbarHost
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.navigateToSafe
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_show.R
import com.michaldrabik.ui_show.ShowDetailsViewModel
import com.michaldrabik.ui_show.databinding.FragmentShowDetailsSeasonsBinding
import com.michaldrabik.ui_show.episodes.ShowDetailsEpisodesFragment
import com.michaldrabik.ui_show.quicksetup.QuickSetupView
import com.michaldrabik.ui_show.sections.seasons.recycler.SeasonListItem
import com.michaldrabik.ui_show.sections.seasons.recycler.SeasonsAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration

@AndroidEntryPoint
class ShowDetailsSeasonsFragment : BaseFragment<ShowDetailsSeasonsViewModel>(R.layout.fragment_show_details_seasons) {

  override val navigationId = R.id.showDetailsFragment
  private val binding by viewBinding(FragmentShowDetailsSeasonsBinding::bind)

  private val parentViewModel by viewModels<ShowDetailsViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<ShowDetailsSeasonsViewModel>()

  private var seasonsAdapter: SeasonsAdapter? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

    launchAndRepeatStarted(
      { parentViewModel.parentEvents.collect { viewModel.handleEvent(it) } },
      { parentViewModel.parentShowState.collect { it?.let { viewModel.loadSeasons(it) } } },
      { viewModel.uiState.collect { render(it) } },
      { viewModel.eventFlow.collect { handleEvent(it as ShowDetailsSeasonsEvent<*>) } }
    )
  }

  override fun onResume() {
    viewModel.refreshSeasons()
    super.onResume()
  }

  private fun setupView() {
    seasonsAdapter = SeasonsAdapter(
      itemClickListener = { viewModel.openSeasonEpisodes(it) },
      itemCheckedListener = { item: SeasonListItem, isChecked: Boolean -> viewModel.setSeasonWatched(item.season, isChecked) }
    )
    binding.showDetailsSeasonsRecycler.apply {
      adapter = seasonsAdapter
      layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
      itemAnimator = null
    }
  }

  private fun render(uiState: ShowDetailsSeasonsUiState) {
    with(uiState) {
      seasons?.let {
        renderSeasons(it)
        renderRuntimeLeft(it)
        (requireAppContext() as WidgetsProvider).requestShowsWidgetsUpdate()
      }
    }
  }

  private fun renderSeasons(seasonsItems: List<SeasonListItem>) {
    with(binding) {
      seasonsAdapter?.setItems(seasonsItems)
      showDetailsSeasonsProgress.gone()
      showDetailsSeasonsEmptyView.visibleIf(seasonsItems.isEmpty())
      showDetailsSeasonsRecycler.fadeIf(seasonsItems.isNotEmpty(), hardware = true)
      showDetailsSeasonsLabel.fadeIf(seasonsItems.isNotEmpty(), hardware = true)
      showDetailsQuickProgress.fadeIf(seasonsItems.isNotEmpty(), hardware = true)
      showDetailsQuickProgress.onClick {
        if (seasonsItems.any { !it.season.isSpecial() }) {
          openQuickSetupDialog(seasonsItems.map { it.season })
        } else {
          showSnack(MessageEvent.Info(R.string.textSeasonsEmpty))
        }
      }
      (requireAppContext() as WidgetsProvider).requestShowsWidgetsUpdate()
    }
  }

  private fun renderRuntimeLeft(seasonsItems: List<SeasonListItem>) {
    val runtimeLeft = seasonsItems
      .filter { !it.season.isSpecial() }
      .flatMap { it.episodes }
      .filterNot { it.isWatched }
      .sumOf { it.episode.runtime }
      .toLong()

    val duration = Duration.ofMinutes(runtimeLeft)
    val hours = duration.toHours()
    val minutes = duration.minusHours(hours).toMinutes()

    val runtimeText = when {
      hours <= 0 -> getString(R.string.textRuntimeLeftMinutes, minutes.toString())
      else -> getString(R.string.textRuntimeLeftHours, hours.toString(), minutes.toString())
    }
    with(binding) {
      showDetailsRuntimeLeft.text = runtimeText
      showDetailsRuntimeLeft.fadeIf(seasonsItems.isNotEmpty() && runtimeLeft > 0, hardware = true)
    }
  }

  private fun handleEvent(event: ShowDetailsSeasonsEvent<*>) {
    when (event) {
      is ShowDetailsSeasonsEvent.RemoveFromTrakt ->
        openRemoveTraktSheet(event)
      is ShowDetailsSeasonsEvent.OpenSeasonEpisodes -> {
        val bundle = ShowDetailsEpisodesFragment.createBundle(event.showId, event.seasonId)
        navigateToSafe(R.id.actionShowDetailsFragmentToEpisodes, bundle)
      }
    }
  }

  private fun openQuickSetupDialog(seasons: List<Season>) {
    val context = requireContext()
    val view = QuickSetupView(context).apply {
      bind(seasons)
    }
    MaterialAlertDialogBuilder(context, R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(context, R.drawable.bg_dialog))
      .setView(view)
      .setPositiveButton(R.string.textSelect) { _, _ ->
        viewModel.setQuickProgress(view.getSelectedItem())
      }
      .setNegativeButton(R.string.textCancel) { _, _ -> }
      .show()
  }

  private fun openRemoveTraktSheet(event: ShowDetailsSeasonsEvent.RemoveFromTrakt) {
    requireParentFragment().setFragmentResultListener(NavigationArgs.REQUEST_REMOVE_TRAKT) { _, bundle ->
      if (bundle.getBoolean(NavigationArgs.RESULT, false)) {
        val text = resources.getString(R.string.textTraktSyncRemovedFromTrakt)
        (requireActivity() as SnackbarHost).provideSnackbarLayout().showInfoSnackbar(text)

        if (event.actionId == R.id.actionShowDetailsFragmentToRemoveTraktProgress) {
          viewModel.refreshSeasons()
        }
      }
    }
    val args = RemoveTraktBottomSheet.createBundle(event.traktIds, event.mode)
    navigateToSafe(event.actionId, args)
  }

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    seasonsAdapter = null
    super.onDestroyView()
  }
}
