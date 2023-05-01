package com.michaldrabik.ui_movie.sections.collections

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.addDivider
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_movie.MovieDetailsFragment
import com.michaldrabik.ui_movie.MovieDetailsViewModel
import com.michaldrabik.ui_movie.R
import com.michaldrabik.ui_movie.databinding.FragmentMovieDetailsCollectionBinding
import com.michaldrabik.ui_movie.sections.collections.recycler.MovieCollectionAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieDetailsCollectionFragment : BaseFragment<MovieDetailsCollectionViewModel>(R.layout.fragment_movie_details_collection) {

  private val parentViewModel by viewModels<MovieDetailsViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<MovieDetailsCollectionViewModel>()

  private val binding by viewBinding(FragmentMovieDetailsCollectionBinding::bind)

  private var collectionsAdapter: MovieCollectionAdapter? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    launchAndRepeatStarted(
      { parentViewModel.parentMovieState.collect { it?.let { viewModel.loadMovieCollections(it) } } },
      { viewModel.uiState.collect { render(it) } }
    )
  }

  private fun setupView() {
    collectionsAdapter = MovieCollectionAdapter(
      itemClickListener = {
//        val bundle = Bundle().apply { putLong(ARG_MOVIE_ID, it.movie.traktId) }
//        navigateTo(R.id.actionMovieDetailsFragmentToSelf, bundle)
      },
    )
    binding.movieDetailsCollectionRecycler.apply {
      setHasFixedSize(true)
      adapter = collectionsAdapter
      layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL, false)
      addDivider(R.drawable.divider_horizontal_list, HORIZONTAL)
    }
  }

  private fun render(uiState: MovieDetailsCollectionUiState) {
    with(uiState) {
      collections?.let {
        collectionsAdapter?.setItems(it)
        if (it.isNotEmpty()) {
          (requireParentFragment() as MovieDetailsFragment).showCollectionsView(animate = true)
        }
      }
      isLoading.let {
        binding.movieDetailsCollectionProgress.visibleIf(it)
      }
    }
  }

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    collectionsAdapter = null
    super.onDestroyView()
  }
}
