package com.michaldrabik.ui_movie.related

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.addDivider
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_movie.R
import com.michaldrabik.ui_movie.related.recycler.RelatedListItem
import com.michaldrabik.ui_movie.related.recycler.RelatedMovieAdapter
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_movie_details_related.*

@AndroidEntryPoint
class MovieDetailsRelatedFragment : BaseFragment<MovieDetailsRelatedViewModel>(R.layout.fragment_movie_details_related) {

  override val viewModel by viewModels<MovieDetailsRelatedViewModel>()

  private var relatedAdapter: RelatedMovieAdapter? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    launchAndRepeatStarted(
//      { viewModel.uiState.collect { render(it) } },
//      { viewModel.messageFlow.collect { renderSnack(it) } },
//      doAfterLaunch = {
//        if (!isInitialized) {
//          viewModel.loadDetails(movieId)
//          isInitialized = true
//        }
//        viewModel.loadPremium()
//        lastOpenedPerson?.let { openPersonSheet(it) }
//      }
    )
  }

  private fun setupView() {
    relatedAdapter = RelatedMovieAdapter(
      itemClickListener = {
        val bundle = Bundle().apply { putLong(ARG_MOVIE_ID, it.movie.ids.trakt.id) }
        navigateTo(R.id.actionMovieDetailsFragmentToSelf, bundle)
      },
      missingImageListener = { relatedListItem, b -> }
    )
//      missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
    movieDetailsRelatedRecycler.apply {
      setHasFixedSize(true)
      adapter = relatedAdapter
      layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL, false)
      addDivider(R.drawable.divider_horizontal_list, HORIZONTAL)
    }
  }

  private fun renderRelatedMovies(items: List<RelatedListItem>) {
    relatedAdapter?.setItems(items)
    movieDetailsRelatedRecycler.visibleIf(items.isNotEmpty())
    movieDetailsRelatedLabel.fadeIf(items.isNotEmpty(), hardware = true)
    movieDetailsRelatedProgress.gone()
  }

  override fun onDestroyView() {
    relatedAdapter = null
    super.onDestroyView()
  }
}
