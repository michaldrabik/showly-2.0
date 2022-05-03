package com.michaldrabik.ui_movie.related

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.addDivider
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_movie.MovieDetailsViewModel
import com.michaldrabik.ui_movie.R
import com.michaldrabik.ui_movie.related.recycler.RelatedMovieAdapter
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_movie_details_related.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MovieDetailsRelatedFragment : BaseFragment<MovieDetailsRelatedViewModel>(R.layout.fragment_movie_details_related) {

  private val parentViewModel by viewModels<MovieDetailsViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<MovieDetailsRelatedViewModel>()

  private var relatedAdapter: RelatedMovieAdapter? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    launchAndRepeatStarted(
      { parentViewModel.eventFlow.collect { viewModel.handleEvent(it) } },
      { viewModel.uiState.collect { render(it) } }
    )
  }

  private fun setupView() {
    relatedAdapter = RelatedMovieAdapter(
      itemClickListener = {
        val bundle = Bundle().apply { putLong(ARG_MOVIE_ID, it.movie.ids.trakt.id) }
        navigateTo(R.id.actionMovieDetailsFragmentToSelf, bundle)
      },
      missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
    )
    movieDetailsRelatedRecycler.apply {
      setHasFixedSize(true)
      adapter = relatedAdapter
      layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL, false)
      addDivider(R.drawable.divider_horizontal_list, HORIZONTAL)
    }
  }

  private fun render(uiState: MovieDetailsRelatedUiState) {
    with(uiState) {
      relatedMovies?.let {
        relatedAdapter?.setItems(it)
        movieDetailsRelatedRecycler.visibleIf(it.isNotEmpty())
        movieDetailsRelatedLabel.fadeIf(it.isNotEmpty(), hardware = true)
      }
      isLoading.let {
        movieDetailsRelatedProgress.visibleIf(it)
      }
    }
  }

  override fun onDestroyView() {
    relatedAdapter = null
    super.onDestroyView()
  }
}
