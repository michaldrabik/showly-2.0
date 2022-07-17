package com.michaldrabik.ui_show.sections.ratings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.openImdbUrl
import com.michaldrabik.ui_base.utilities.extensions.openWebUrl
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.IdImdb
import com.michaldrabik.ui_show.R
import com.michaldrabik.ui_show.ShowDetailsViewModel
import com.michaldrabik.ui_show.databinding.FragmentShowDetailsRatingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowDetailsRatingsFragment : BaseFragment<ShowDetailsRatingsViewModel>(R.layout.fragment_show_details_ratings) {

  override val navigationId = R.id.showDetailsFragment
  private val binding by viewBinding(FragmentShowDetailsRatingsBinding::bind)

  private val parentViewModel by viewModels<ShowDetailsViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<ShowDetailsRatingsViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    launchAndRepeatStarted(
      { parentViewModel.parentShowState.collect { it?.let { viewModel.loadRatings(it) } } },
      { viewModel.uiState.collect { render(it) } }
    )
  }

  private fun render(uiState: ShowDetailsRatingsUiState) {
    with(uiState) {
      with(binding) {
        ratings?.let {
          if (showDetailsRatings.isBound()) return
          showDetailsRatings.bind(ratings)
          show?.let {
            showDetailsRatings.onTraktClick = { openLink(ShowLink.TRAKT, show.traktId.toString()) }
            showDetailsRatings.onImdbClick = { openLink(ShowLink.IMDB, show.ids.imdb.id) }
            showDetailsRatings.onMetaClick = { openLink(ShowLink.METACRITIC, show.title) }
            showDetailsRatings.onRottenClick = {
              val url = it.rottenTomatoesUrl
              if (!url.isNullOrBlank()) {
                openWebUrl(url) ?: openLink(ShowLink.ROTTEN, "${show.title} ${show.year}")
              } else {
                openLink(ShowLink.ROTTEN, "${show.title} ${show.year}")
              }
            }
          }
        }
      }
    }
  }

  private fun openLink(
    link: ShowLink,
    id: String,
    country: AppCountry = AppCountry.UNITED_STATES,
  ) {
    if (link == ShowLink.IMDB) {
      openImdbUrl(IdImdb(id)) ?: showSnack(MessageEvent.Info(R.string.errorCouldNotFindApp))
    } else {
      openWebUrl(link.getUri(id, country)) ?: showSnack(MessageEvent.Info(R.string.errorCouldNotFindApp))
    }
  }

  override fun setupBackPressed() = Unit
}
