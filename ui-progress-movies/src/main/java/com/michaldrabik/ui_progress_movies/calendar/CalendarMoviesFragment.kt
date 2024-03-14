package com.michaldrabik.ui_progress_movies.calendar

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.repository.settings.SettingsViewModeRepository
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.OnSearchClickListener
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withSpanSizeLookup
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.CalendarMode.PRESENT_FUTURE
import com.michaldrabik.ui_model.CalendarMode.RECENTS
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.calendar.recycler.CalendarMovieListItem
import com.michaldrabik.ui_progress_movies.calendar.recycler.CalendarMoviesAdapter
import com.michaldrabik.ui_progress_movies.databinding.FragmentCalendarMoviesBinding
import com.michaldrabik.ui_progress_movies.helpers.ProgressMoviesLayoutManagerProvider
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesMainFragment
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesMainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CalendarMoviesFragment :
  BaseFragment<CalendarMoviesViewModel>(R.layout.fragment_calendar_movies),
  OnSearchClickListener,
  OnScrollResetListener {

  @Inject lateinit var settings: SettingsViewModeRepository

  private val parentViewModel by viewModels<ProgressMoviesMainViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<CalendarMoviesViewModel>()

  private val binding by viewBinding(FragmentCalendarMoviesBinding::bind)

  private var adapter: CalendarMoviesAdapter? = null
  private var layoutManager: LayoutManager? = null
  private var statusBarHeight = 0
  private var isSearching = false

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupRecycler()
    setupStatusBar()

    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(parentViewModel) {
          launch { uiState.collect { viewModel.onParentState(it) } }
        }
        with(viewModel) {
          launch { uiState.collect { render(it) } }
        }
      }
    }
  }

  private fun setupRecycler() {
    val gridSpanSize = settings.tabletGridSpanSize
    layoutManager = ProgressMoviesLayoutManagerProvider.provideLayoutManger(requireContext(), gridSpanSize)
    (layoutManager as? GridLayoutManager)?.run {
      withSpanSizeLookup { position ->
        when (adapter?.getItems()?.get(position)) {
          is CalendarMovieListItem.Header -> gridSpanSize
          is CalendarMovieListItem.Filters -> gridSpanSize
          is CalendarMovieListItem.MovieItem -> 1
          else -> throw IllegalStateException()
        }
      }
    }
    adapter = CalendarMoviesAdapter(
      itemClickListener = { requireMainFragment().openMovieDetails(it.movie) },
      itemLongClickListener = { requireMainFragment().openMovieMenu(it.movie, showPinButtons = false) },
      missingImageListener = { item, force -> viewModel.findMissingImage(item, force) },
      missingTranslationListener = { item -> viewModel.findMissingTranslation(item) },
      modeClickListener = { requireMainFragment().toggleCalendarMode() },
    )
    binding.progressMoviesCalendarRecycler.apply {
      adapter = this@CalendarMoviesFragment.adapter
      layoutManager = this@CalendarMoviesFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      binding.progressMoviesCalendarRecycler.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.progressMoviesCalendarTabsViewPadding))
      return
    }
    binding.progressMoviesCalendarRecycler.doOnApplyWindowInsets { view, insets, _, _ ->
      val tabletOffset = if (isTablet) dimenToPx(R.dimen.spaceMedium) else 0
      statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top + tabletOffset
      view.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.progressMoviesCalendarTabsViewPadding))
    }
  }

  override fun onScrollReset() = binding.progressMoviesCalendarRecycler.smoothScrollToPosition(0)

  override fun onEnterSearch() {
    isSearching = true

    binding.progressMoviesCalendarRecycler.translationY = dimenToPx(R.dimen.progressMoviesSearchLocalOffset).toFloat()
    binding.progressMoviesCalendarRecycler.smoothScrollToPosition(0)
  }

  override fun onExitSearch() {
    isSearching = false

    binding.progressMoviesCalendarRecycler.translationY = 0F
    binding.progressMoviesCalendarRecycler.smoothScrollToPosition(0)
  }

  private fun render(uiState: CalendarMoviesUiState) {
    with(binding) {
      uiState.run {
        items?.let {
          adapter?.setItems(it)
          progressMoviesCalendarRecycler.fadeIn(150, withHardware = true)
          val anyMovie = items.any { item -> item is CalendarMovieListItem.MovieItem }
          progressMoviesCalendarEmptyFutureView.rootLayout.visibleIf(!anyMovie && mode == PRESENT_FUTURE && !isSearching)
          progressMoviesCalendarEmptyRecentsView.rootLayout.visibleIf(!anyMovie && mode == RECENTS && !isSearching)
        }
      }
    }
  }

  private fun requireMainFragment() = (requireParentFragment() as ProgressMoviesMainFragment)

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
