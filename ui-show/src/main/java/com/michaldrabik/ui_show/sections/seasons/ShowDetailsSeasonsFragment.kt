package com.michaldrabik.ui_show.sections.seasons

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.repository.settings.SettingsViewModeRepository
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.WidgetsProvider
import com.michaldrabik.ui_base.common.sheets.date_selection.DateSelectionBottomSheet.Companion.REQUEST_DATE_SELECTION
import com.michaldrabik.ui_base.common.sheets.date_selection.DateSelectionBottomSheet.Companion.RESULT_DATE_SELECTION
import com.michaldrabik.ui_base.common.sheets.date_selection.DateSelectionBottomSheet.Result
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.utilities.SnackbarHost
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.navigateToSafe
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.requireParcelable
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_REMOVE_TRAKT
import com.michaldrabik.ui_navigation.java.NavigationArgs.RESULT
import com.michaldrabik.ui_show.R
import com.michaldrabik.ui_show.ShowDetailsViewModel
import com.michaldrabik.ui_show.databinding.FragmentShowDetailsSeasonsBinding
import com.michaldrabik.ui_show.episodes.ShowDetailsEpisodesFragment
import com.michaldrabik.ui_show.quicksetup.QuickSetupListItem
import com.michaldrabik.ui_show.quicksetup.QuickSetupView
import com.michaldrabik.ui_show.sections.seasons.ShowDetailsSeasonsEvent.OpenQuickProgressDateSelection
import com.michaldrabik.ui_show.sections.seasons.ShowDetailsSeasonsEvent.OpenSeasonDateSelection
import com.michaldrabik.ui_show.sections.seasons.ShowDetailsSeasonsEvent.OpenSeasonEpisodes
import com.michaldrabik.ui_show.sections.seasons.ShowDetailsSeasonsEvent.RemoveFromTrakt
import com.michaldrabik.ui_show.sections.seasons.ShowDetailsSeasonsEvent.RequestWidgetsUpdate
import com.michaldrabik.ui_show.sections.seasons.recycler.SeasonListItem
import com.michaldrabik.ui_show.sections.seasons.recycler.SeasonsAdapter
import com.michaldrabik.ui_show.sections.seasons.recycler.helpers.SeasonsGridItemDecoration
import com.michaldrabik.ui_show.sections.seasons.recycler.helpers.SeasonsLayoutManagerProvider
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import javax.inject.Inject

@AndroidEntryPoint
class ShowDetailsSeasonsFragment : BaseFragment<ShowDetailsSeasonsViewModel>(R.layout.fragment_show_details_seasons) {

  @Inject lateinit var settings: SettingsViewModeRepository

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
      itemCheckedListener = { item: SeasonListItem, isChecked: Boolean ->
        viewModel.onSeasonChecked(item.season, isChecked)
      }
    )
    binding.showDetailsSeasonsRecycler.apply {
      adapter = seasonsAdapter
      layoutManager = SeasonsLayoutManagerProvider.provideLayoutManger(requireContext(), settings)
      itemAnimator = null
      if (layoutManager is GridLayoutManager) {
        addItemDecoration(SeasonsGridItemDecoration(requireContext(), R.dimen.spaceBig))
      }
    }
    binding.showDetailsSeasonsLabel.text = getString(R.string.textSeasons).replace(":", "")
  }

  private fun render(uiState: ShowDetailsSeasonsUiState) {
    with(uiState) {
      seasons?.let {
        renderSeasons(it)
        renderRuntimeLeft(it)
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
      is RemoveFromTrakt -> openRemoveTraktSheet(event)
      is OpenSeasonDateSelection -> openDateSelectionDialog(event.season)
      is OpenQuickProgressDateSelection -> openDateSelectionDialog(event.item)
      is OpenSeasonEpisodes -> {
        val bundle = ShowDetailsEpisodesFragment.createBundle(event.showId, event.seasonId)
        navigateToSafe(R.id.actionShowDetailsFragmentToEpisodes, bundle)
      }
      is RequestWidgetsUpdate -> (requireAppContext() as WidgetsProvider).requestShowsWidgetsUpdate()
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
      .setPositiveButton(R.string.textSelect) { _, _ -> viewModel.onQuickProgressSelected(view.getSelectedItem()) }
      .setNegativeButton(R.string.textCancel) { _, _ -> }
      .show()
  }

  private fun openRemoveTraktSheet(event: RemoveFromTrakt) {
    requireParentFragment().setFragmentResultListener(REQUEST_REMOVE_TRAKT) { _, bundle ->
      if (bundle.getBoolean(RESULT, false)) {
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

  private fun openDateSelectionDialog(season: Season) {
    requireParentFragment().setFragmentResultListener(REQUEST_DATE_SELECTION) { _, bundle ->
      when (val result = bundle.requireParcelable<Result>(RESULT_DATE_SELECTION)) {
        is Result.Now -> viewModel.setSeasonWatched(season, true)
        is Result.CustomDate -> viewModel.setSeasonWatched(season, true, result.date)
      }
    }
    navigateToSafe(R.id.actionShowDetailsFragmentToDateSelection)
  }

  private fun openDateSelectionDialog(item: QuickSetupListItem) {
    requireParentFragment().setFragmentResultListener(REQUEST_DATE_SELECTION) { _, bundle ->
      when (val result = bundle.requireParcelable<Result>(RESULT_DATE_SELECTION)) {
        is Result.Now -> viewModel.setQuickProgress(item, null)
        is Result.CustomDate -> viewModel.setQuickProgress(item, result.date)
      }
    }
    navigateToSafe(R.id.actionShowDetailsFragmentToDateSelection)
  }

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    seasonsAdapter = null
    super.onDestroyView()
  }
}
