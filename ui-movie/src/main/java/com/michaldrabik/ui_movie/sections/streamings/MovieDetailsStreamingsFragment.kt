package com.michaldrabik.ui_movie.sections.streamings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.addDivider
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_movie.MovieDetailsFragment
import com.michaldrabik.ui_movie.MovieDetailsViewModel
import com.michaldrabik.ui_movie.R
import com.michaldrabik.ui_streamings.recycler.StreamingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_movie_details_streamings.*

@AndroidEntryPoint
class MovieDetailsStreamingsFragment : BaseFragment<MovieDetailsStreamingsViewModel>(R.layout.fragment_movie_details_streamings) {

  private val parentViewModel by viewModels<MovieDetailsViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<MovieDetailsStreamingsViewModel>()

  private var streamingAdapter: StreamingAdapter? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    launchAndRepeatStarted(
      { parentViewModel.parentMovieState.collect { it?.let { viewModel.loadStreamings(it) } } },
      { viewModel.uiState.collect { render(it) } }
    )
  }

  private fun setupView() {
    streamingAdapter = StreamingAdapter()
    movieDetailsStreamingsRecycler.apply {
      setHasFixedSize(true)
      adapter = streamingAdapter
      layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL, false)
      addDivider(R.drawable.divider_horizontal_list, HORIZONTAL)
    }
  }

  private fun render(uiState: MovieDetailsStreamingsUiState) {
    with(uiState) {
      streamings?.let {
        if (streamingAdapter?.itemCount != 0) return@let
        val (items, isLocal) = it
        streamingAdapter?.setItems(items)
        if (items.isNotEmpty()) {
          (requireParentFragment() as MovieDetailsFragment).showStreamingsView(animate = !isLocal)
        }
      }
    }
  }

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    streamingAdapter = null
    super.onDestroyView()
  }
}
