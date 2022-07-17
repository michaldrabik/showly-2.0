package com.michaldrabik.ui_show.episodes

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.WidgetsProvider
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet.Options.Operation
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet.Options.Type
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.utilities.SnackbarHost
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.navigateToSafe
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.setCheckedSilent
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_episodes.details.EpisodeDetailsBottomSheet
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_show.R
import com.michaldrabik.ui_show.databinding.FragmentShowDetailsEpisodesBinding
import com.michaldrabik.ui_show.episodes.recycler.EpisodesAdapter
import com.michaldrabik.ui_show.sections.seasons.recycler.SeasonListItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.util.Locale

@AndroidEntryPoint
class ShowDetailsEpisodesFragment : BaseFragment<ShowDetailsEpisodesViewModel>(R.layout.fragment_show_details_episodes) {

  companion object {
    fun createBundle(
      showId: IdTrakt,
      seasonId: IdTrakt
    ): Bundle = bundleOf(
      NavigationArgs.ARG_OPTIONS to Options(showId, seasonId)
    )
  }

  override val navigationId = R.id.showDetailsEpisodesFragment
  private val binding by viewBinding(FragmentShowDetailsEpisodesBinding::bind)

  override val viewModel by viewModels<ShowDetailsEpisodesViewModel>()

  private var episodesAdapter: EpisodesAdapter? = null
  private var isLocked = true

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupView()
    setupStatusBar()
    setupRecycler()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.eventFlow.collect { handleEvent(it as ShowDetailsEpisodesEvent<*>) } }
    )
  }

  override fun onResume() {
    super.onResume()
    viewModel.launchRefreshWatchedEpisodes()
  }

  private fun setupView() {
    with(binding) {
      episodesBackArrow.onClick { requireActivity().onBackPressed() }
      episodesUnlockButton.onClick(safe = false) { toggleEpisodesLock() }
      listOf(episodesSeasonRateButton, episodesSeasonMyStarIcon).onClick {
        viewModel.openRateSeasonDialog()
      }
    }
  }

  private fun setupStatusBar() {
    with(binding) {
      episodesRoot.doOnApplyWindowInsets { _, insets, padding, _ ->
        val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
        episodesRoot.updatePadding(top = padding.top + inset)
      }
    }
  }

  private fun setupRecycler() {
    episodesAdapter = EpisodesAdapter(
      itemClickListener = { episode: Episode, isWatched: Boolean ->
        viewModel.openEpisodeDetails(episode, isWatched)
      },
      itemCheckedListener = { episode: Episode, isChecked: Boolean ->
        viewModel.setEpisodeWatched(episode, isChecked)
      }
    )
    binding.episodesRecycler.apply {
      setHasFixedSize(true)
      adapter = episodesAdapter
      layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
      itemAnimator = null
    }
  }

  private fun render(uiState: ShowDetailsEpisodesUiState) {
    with(uiState) {
      with(binding) {
        season?.let {
          episodesTitle.text =
            if (it.season.isSpecial()) getString(R.string.textSpecials)
            else String.format(Locale.ENGLISH, getString(R.string.textSeason), it.season.number)
          episodesOverview.text = it.season.overview
          episodesOverview.visibleIf(it.season.overview.isNotBlank())
          episodesCheckbox.run {
            isEnabled = it.episodes.all { ep -> ep.episode.hasAired(it.season) } || !isLocked
            setCheckedSilent(it.isWatched) { _, isChecked ->
              viewModel.setSeasonWatched(season, isChecked)
            }
            jumpDrawablesToCurrentState()
          }
          episodesUnlockButton.visibleIf(!it.season.isSpecial() && it.episodes.any { ep -> !ep.episode.hasAired(it.season) })

          renderSeasonRating(season)
        }
        episodes?.let {
          episodesAdapter?.setItems(it)
          if (isInitialLoad == true) {
            episodesRecycler.scheduleLayoutAnimation()
          }
          (requireAppContext() as WidgetsProvider).requestShowsWidgetsUpdate()
        }
      }
    }
  }

  private fun renderSeasonRating(season: SeasonListItem) {
    with(binding) {
      val seasonRating = season.season.rating
      episodesStarIcon.visibleIf(seasonRating > 0F)
      episodesSeasonRating.visibleIf(seasonRating > 0F)
      episodesSeasonRating.text = String.format(Locale.ENGLISH, "%.1f", seasonRating)

      val ratingState = season.userRating
      episodesSeasonRateButton.visibleIf(ratingState.rateAllowed == true && ratingState.userRating == null)
      episodesSeasonMyStarIcon.visibleIf(ratingState.userRating != null)
      episodesSeasonMyRating.visibleIf(ratingState.userRating != null)
      ratingState.userRating?.let {
        episodesSeasonMyStarIcon.isEnabled = ratingState.rateAllowed == true
        episodesSeasonMyRating.text = String.format(Locale.ENGLISH, "%d", it.rating)
      }
    }
  }

  private fun handleEvent(event: ShowDetailsEpisodesEvent<*>) {
    when (event) {
      is ShowDetailsEpisodesEvent.Finish -> requireActivity().onBackPressed()
      is ShowDetailsEpisodesEvent.RemoveFromTrakt -> openRemoveTraktSheet(event)
      is ShowDetailsEpisodesEvent.OpenEpisodeDetails -> openEpisodeDetails(event.bundle, event.isWatched)
      is ShowDetailsEpisodesEvent.OpenRateSeason -> openRateSeasonDialog(event.season)
    }
  }

  private fun toggleEpisodesLock() {
    isLocked = !isLocked
    with(binding) {
      episodesUnlockButton.setImageResource(if (isLocked) R.drawable.ic_locked else R.drawable.ic_unlocked)
      episodesCheckbox.isEnabled = !isLocked
    }
    episodesAdapter?.toggleEpisodesLock()
  }

  private fun openEpisodeDetails(
    episodeBundle: EpisodeBundle,
    isWatched: Boolean
  ) {
    val (episode, season, show) = episodeBundle
    setFragmentResultListener(NavigationArgs.REQUEST_EPISODE_DETAILS) { _, bundle ->
      when {
        bundle.containsKey(NavigationArgs.ACTION_EPISODE_WATCHED) -> {
          val watched = bundle.getBoolean(NavigationArgs.ACTION_EPISODE_WATCHED)
          viewModel.setEpisodeWatched(episode, watched)
        }
        bundle.containsKey(NavigationArgs.ACTION_EPISODE_TAB_SELECTED) -> {
          val selectedEpisode = bundle.getParcelable<Episode>(NavigationArgs.ACTION_EPISODE_TAB_SELECTED)!!
          viewModel.openEpisodeDetails(selectedEpisode)
        }
        bundle.containsKey(NavigationArgs.ACTION_RATING_CHANGED) -> {
          viewModel.loadEpisodesRating()
        }
      }
    }

    val bundle = EpisodeDetailsBottomSheet.createBundle(
      ids = show.ids,
      episode = episode,
      seasonEpisodesIds = season.episodes.map { it.number },
      isWatched = isWatched,
      showButton = episode.hasAired(season),
      showTabs = true
    )
    navigateToSafe(R.id.actionEpisodesFragmentToEpisodesDetails, bundle)
  }

  private fun openRemoveTraktSheet(event: ShowDetailsEpisodesEvent.RemoveFromTrakt) {
    setFragmentResultListener(NavigationArgs.REQUEST_REMOVE_TRAKT) { _, bundle ->
      if (bundle.getBoolean(NavigationArgs.RESULT, false)) {
        val text = resources.getString(R.string.textTraktSyncRemovedFromTrakt)
        (requireActivity() as SnackbarHost).provideSnackbarLayout().showInfoSnackbar(text)
      }
    }
    val args = RemoveTraktBottomSheet.createBundle(event.traktIds, event.mode)
    navigateToSafe(event.actionId, args)
  }

  private fun openRateSeasonDialog(season: Season) {
    setFragmentResultListener(NavigationArgs.REQUEST_RATING) { _, bundle ->
      when (bundle.getParcelable<Operation>(NavigationArgs.RESULT)) {
        Operation.SAVE -> showSnack(MessageEvent.Info(R.string.textRateSaved))
        Operation.REMOVE -> showSnack(MessageEvent.Info(R.string.textRateRemoved))
        else -> Timber.w("Unknown result")
      }
      viewModel.loadSeasonRating()
    }
    val bundle = RatingsBottomSheet.createBundle(season.ids.trakt, Type.SEASON)
    navigateToSafe(R.id.actionEpisodesFragmentToRating, bundle)
  }

  override fun onDestroyView() {
    episodesAdapter = null
    super.onDestroyView()
  }

  @Parcelize
  data class Options(
    val showId: IdTrakt,
    val seasonId: IdTrakt
  ) : Parcelable
}
