package com.michaldrabik.ui_show.sections.episodes

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
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.navigateToSafe
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.setCheckedSilent
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_episodes.details.EpisodeDetailsBottomSheet
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_show.R
import com.michaldrabik.ui_show.databinding.FragmentShowDetailsEpisodesBinding
import com.michaldrabik.ui_show.sections.episodes.recycler.EpisodesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.parcelize.Parcelize
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

  private fun setupView() {
    with(binding) {
      episodesBackArrow.onClick { requireActivity().onBackPressed() }
      episodesUnlockButton.onClick(safe = false) { toggleEpisodesLock() }
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
      itemClickListener = { episode: Episode, b: Boolean ->
//        openEpisodeDetails()
      },
      itemCheckedListener = { episode: Episode, isChecked: Boolean ->
//        viewModel.setEpisodeWatched(episode, season, isChecked, removeTrakt = true)
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
//              seasonCheckedListener(season, isChecked)
            }
            jumpDrawablesToCurrentState()
          }
          episodesUnlockButton.visibleIf(!it.season.isSpecial() && it.episodes.any { ep -> !ep.episode.hasAired(it.season) })
        }
        episodes?.let {
          episodesAdapter?.setItems(it)
          episodesRecycler.scheduleLayoutAnimation()
        }
      }
    }
  }

  private fun handleEvent(event: ShowDetailsEpisodesEvent<*>) {
    when (event) {
      ShowDetailsEpisodesEvent.Finish -> requireActivity().onBackPressed()
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

  fun openEpisodeDetails(
    show: Show,
    episode: Episode,
    season: Season?,
    isWatched: Boolean,
    showButton: Boolean = true,
    showTabs: Boolean = true,
  ) {
    if (season !== null) {
      setFragmentResultListener(NavigationArgs.REQUEST_EPISODE_DETAILS) { _, bundle ->
        when {
          bundle.containsKey(NavigationArgs.ACTION_RATING_CHANGED) -> {
//            viewModel.refreshEpisodesRatings()
          }
          bundle.containsKey(NavigationArgs.ACTION_EPISODE_WATCHED) -> {
            val watched = bundle.getBoolean(NavigationArgs.ACTION_EPISODE_WATCHED)
//            viewModel.setEpisodeWatched(episode, season, watched, removeTrakt = true)
          }
          bundle.containsKey(NavigationArgs.ACTION_EPISODE_TAB_SELECTED) -> {
            val selectedEpisode = bundle.getParcelable<Episode>(NavigationArgs.ACTION_EPISODE_TAB_SELECTED)!!
//            binding.showDetailsEpisodesView.selectEpisode(selectedEpisode)
          }
        }
      }
    }
    val bundle = Bundle().apply {
      val seasonEpisodes = season?.episodes?.map { it.number }?.toIntArray()
      putLong(EpisodeDetailsBottomSheet.ARG_ID_TRAKT, show.traktId)
      putLong(EpisodeDetailsBottomSheet.ARG_ID_TMDB, show.ids.tmdb.id)
      putParcelable(EpisodeDetailsBottomSheet.ARG_EPISODE, episode)
      putIntArray(EpisodeDetailsBottomSheet.ARG_SEASON_EPISODES, seasonEpisodes)
      putBoolean(EpisodeDetailsBottomSheet.ARG_IS_WATCHED, isWatched)
      putBoolean(EpisodeDetailsBottomSheet.ARG_SHOW_BUTTON, showButton)
      putBoolean(EpisodeDetailsBottomSheet.ARG_SHOW_TABS, showTabs)
    }
    navigateToSafe(R.id.actionShowDetailsFragmentEpisodeDetails, bundle)
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
