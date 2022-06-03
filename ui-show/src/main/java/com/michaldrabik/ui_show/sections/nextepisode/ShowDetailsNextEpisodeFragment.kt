package com.michaldrabik.ui_show.sections.nextepisode

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_show.R
import com.michaldrabik.ui_show.ShowDetailsFragment
import com.michaldrabik.ui_show.ShowDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_show_details.*
import kotlinx.android.synthetic.main.fragment_show_details_next_episode.*
import kotlinx.coroutines.flow.collect
import java.util.Locale

@AndroidEntryPoint
class ShowDetailsNextEpisodeFragment : BaseFragment<ShowDetailsNextEpisodeViewModel>(R.layout.fragment_show_details_next_episode) {

  override val navigationId = R.id.showDetailsFragment

  private val parentViewModel by viewModels<ShowDetailsViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<ShowDetailsNextEpisodeViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    launchAndRepeatStarted(
      { parentViewModel.parentEvents.collect { viewModel.handleEvent(it) } },
      { viewModel.uiState.collect { render(it) } }
    )
  }

  private fun render(uiState: ShowDetailsNextEpisodeUiState) {
    with(uiState) {
      nextEpisode?.let {
        val (show, episode) = it.nextEpisode
        showDetailsEpisodeText.text =
          String.format(Locale.ENGLISH, getString(R.string.textEpisodeTitle), episode.season, episode.number, episode.title)

        episode.firstAired?.let { date ->
          val displayDate = it.dateFormat?.format(date.toLocalZone())?.capitalizeWords()
          showDetailsEpisodeAirtime.visible()
          showDetailsEpisodeAirtime.text = displayDate
        }

        showDetailsEpisodeRoot.onClick { openDetails(show, episode) }
        requireParentFragment().showDetailsEpisodeFragment.fadeIn(withHardware = true)
      }
    }
  }

  private fun openDetails(show: Show, episode: Episode) {
    (requireParentFragment() as ShowDetailsFragment)
      .openEpisodeDetails(
        show = show,
        episode = episode,
        season = null,
        isWatched = false,
        showButton = false,
        showTabs = false
      )
  }

  override fun setupBackPressed() = Unit
}
