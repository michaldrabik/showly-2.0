package com.michaldrabik.ui_my_movies.mymovies

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.common.OnTranslationsSyncListener
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.MyMoviesSection
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_my_movies.di.UiMyMoviesComponentProvider
import com.michaldrabik.ui_my_movies.main.FollowedMoviesFragment
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesAdapter
import kotlinx.android.synthetic.main.fragment_my_movies.*

class MyMoviesFragment :
  BaseFragment<MyMoviesViewModel>(R.layout.fragment_my_movies),
  OnScrollResetListener,
  OnTraktSyncListener,
  OnTranslationsSyncListener {

  override val viewModel by viewModels<MyMoviesViewModel> { viewModelFactory }

  private val adapter by lazy { MyMoviesAdapter() }
  private lateinit var layoutManager: LinearLayoutManager
  private var statusBarHeight = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiMyMoviesComponentProvider).provideMyMoviesComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupStatusBar()
    setupRecycler()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
      loadMovies()
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    myMoviesRecycler.apply {
      adapter = this@MyMoviesFragment.adapter
      layoutManager = this@MyMoviesFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
    adapter.run {
      itemClickListener = { openMovieDetails(it.movie) }
      missingImageListener = { item, force -> viewModel.loadMissingImage(item, force) }
      missingTranslationListener = { viewModel.loadMissingTranslation(it) }
      onSortOrderClickListener = { section, order -> showSortOrderDialog(section, order) }
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      myMoviesRoot.updatePadding(top = statusBarHeight)
      return
    }
    myMoviesRoot.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = statusBarHeight)
    }
  }

  private fun showSortOrderDialog(section: MyMoviesSection, order: SortOrder) {
    val options = listOf(NAME, RATING, NEWEST, DATE_ADDED)
    val optionsStrings = options.map { getString(it.displayString) }.toTypedArray()

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setTitle(R.string.textSortBy)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(optionsStrings, options.indexOf(order)) { dialog, index ->
        viewModel.loadSortedSection(section, options[index])
        dialog.dismiss()
      }
      .show()
  }

  private fun render(uiModel: MyMoviesUiModel) {
    uiModel.run {
      listItems?.let {
        adapter.notifyListsUpdate = notifyListsUpdate ?: false
        adapter.setItems(it)
        myMoviesEmptyView.fadeIf(it.isEmpty())
        (parentFragment as FollowedMoviesFragment).enableSearch(it.isNotEmpty())
      }
    }
  }

  private fun openMovieDetails(movie: Movie) {
    (parentFragment as? FollowedMoviesFragment)?.openMovieDetails(movie)
  }

  override fun onScrollReset() = myMoviesRecycler.scrollToPosition(0)

  override fun onTraktSyncProgress() = viewModel.loadMovies()

  override fun onTranslationsSyncProgress() = viewModel.loadMovies()
}
