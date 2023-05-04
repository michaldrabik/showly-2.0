package com.michaldrabik.ui_movie.sections.collections.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.michaldrabik.repository.movies.MovieCollectionsRepository.Source
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.addDivider
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.navigateToSafe
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.MovieCollection
import com.michaldrabik.ui_movie.MovieDetailsFragment
import com.michaldrabik.ui_movie.MovieDetailsViewModel
import com.michaldrabik.ui_movie.R
import com.michaldrabik.ui_movie.databinding.FragmentMovieDetailsCollectionBinding
import com.michaldrabik.ui_movie.sections.collections.details.MovieDetailsCollectionBottomSheet
import com.michaldrabik.ui_movie.sections.collections.list.recycler.MovieCollectionAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieDetailsCollectionsFragment : BaseFragment<MovieDetailsCollectionsViewModel>(R.layout.fragment_movie_details_collection) {

  override val navigationId = R.id.movieDetailsFragment

  private val parentViewModel by viewModels<MovieDetailsViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<MovieDetailsCollectionsViewModel>()

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
      itemClickListener = { openCollectionSheet(it) }
    )
    binding.movieDetailsCollectionRecycler.apply {
      setHasFixedSize(true)
      adapter = collectionsAdapter
      layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL, false)
      addDivider(R.drawable.divider_horizontal_list, HORIZONTAL)
    }
  }

  private fun render(uiState: MovieDetailsCollectionsUiState) {
    with(uiState) {
      collections?.let { (collections, source) ->
        collectionsAdapter?.setItems(collections)
        if (collections.isNotEmpty()) {
          (requireParentFragment() as MovieDetailsFragment).showCollectionsView(animate = source == Source.REMOTE)
        }
      }
      isLoading.let {
        binding.movieDetailsCollectionProgress.visibleIf(it)
      }
    }
  }

  private fun openCollectionSheet(collection: MovieCollection) {
    val bundle = MovieDetailsCollectionBottomSheet.createBundle(collection.id)
    navigateToSafe(R.id.actionMovieDetailsFragmentToCollection, bundle)
  }

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    collectionsAdapter = null
    super.onDestroyView()
  }
}
