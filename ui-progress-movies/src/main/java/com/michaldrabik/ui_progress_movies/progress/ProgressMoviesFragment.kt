package com.michaldrabik.ui_progress_movies.progress

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.OnSearchClickListener
import com.michaldrabik.ui_base.common.WidgetsProvider
import com.michaldrabik.ui_base.common.sheets.sort_order.SortOrderBottomSheet
import com.michaldrabik.ui_base.utilities.NavigationHost
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.extensions.add
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.SortOrder.USER_RATING
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SELECTED_SORT_ORDER
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SELECTED_SORT_TYPE
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_SORT_ORDER
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.helpers.TopOverscrollAdapter
import com.michaldrabik.ui_progress_movies.main.MovieCheckActionUiEvent
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesMainFragment
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesMainViewModel
import com.michaldrabik.ui_progress_movies.progress.recycler.ProgressMoviesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_progress_movies.*
import kotlinx.android.synthetic.main.layout_progress_movies_empty.*
import kotlinx.coroutines.launch
import me.everything.android.ui.overscroll.IOverScrollDecor
import me.everything.android.ui.overscroll.IOverScrollState.STATE_BOUNCE_BACK
import me.everything.android.ui.overscroll.IOverScrollState.STATE_DRAG_START_SIDE
import me.everything.android.ui.overscroll.OverScrollBounceEffectDecoratorBase
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator

@AndroidEntryPoint
class ProgressMoviesFragment :
  BaseFragment<ProgressMoviesViewModel>(R.layout.fragment_progress_movies),
  OnSearchClickListener,
  OnScrollResetListener {

  private companion object {
    const val OVERSCROLL_OFFSET = 250F
    const val OVERSCROLL_OFFSET_TRANSLATION = 4.5F
  }

  private val parentViewModel by viewModels<ProgressMoviesMainViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<ProgressMoviesViewModel>()

  private var adapter: ProgressMoviesAdapter? = null
  private var layoutManager: LinearLayoutManager? = null
  private var statusBarHeight = 0
  private var overscroll: IOverScrollDecor? = null
  private var overscrollEnabled = true
  private var isSearching = false

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()
    setupStatusBar()

    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(parentViewModel) {
          launch { uiState.collect { viewModel.onParentState(it) } }
        }
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          launch { messageFlow.collect { showSnack(it) } }
          launch { eventFlow.collect { handleEvent(it) } }
        }
      }
    }
  }

  private fun setupView() {
    progressMoviesEmptyTraktButton.onClick { requireMainFragment().openTraktSync() }
    progressMoviesEmptyDiscoverButton.onClick {
      (requireActivity() as NavigationHost).navigateToDiscover()
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = ProgressMoviesAdapter(
      itemClickListener = { requireMainFragment().openMovieDetails(it.movie) },
      itemLongClickListener = { requireMainFragment().openMovieMenu(it.movie) },
      sortChipClickListener = ::openSortOrderDialog,
      missingImageListener = viewModel::findMissingImage,
      missingTranslationListener = viewModel::findMissingTranslation,
      checkClickListener = { viewModel.onMovieChecked(it.movie) },
      listChangeListener = {
        requireMainFragment().resetTranslations()
        layoutManager?.scrollToPosition(0)
      }
    )
    progressMoviesMainRecycler.apply {
      adapter = this@ProgressMoviesFragment.adapter
      layoutManager = this@ProgressMoviesFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
  }

  private fun setupOverscroll() {
    if (overscroll != null) return
    val adapt = TopOverscrollAdapter(progressMoviesMainRecycler)
    overscroll = VerticalOverScrollBounceEffectDecorator(
      adapt,
      1.75F,
      OverScrollBounceEffectDecoratorBase.DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK,
      OverScrollBounceEffectDecoratorBase.DEFAULT_DECELERATE_FACTOR
    ).apply {
      setOverScrollUpdateListener { _, state, offset ->
        progressMoviesOverscrollIcon?.run {
          if (offset > 0) {
            val value = (offset / OVERSCROLL_OFFSET).coerceAtMost(1F)
            val valueTranslation = offset / OVERSCROLL_OFFSET_TRANSLATION
            when (state) {
              STATE_DRAG_START_SIDE -> {
                alpha = value
                scaleX = value
                scaleY = value
                translationY = valueTranslation
                overscrollEnabled = true
              }
              STATE_BOUNCE_BACK -> {
                alpha = value
                scaleX = value
                scaleY = value
                translationY = valueTranslation
                if (offset >= OVERSCROLL_OFFSET && overscrollEnabled) {
                  overscrollEnabled = false
                  viewModel.startTraktSync()
                }
              }
            }
          } else {
            alpha = 0F
            scaleX = 0F
            scaleY = 0F
            translationY = 0F
          }
        }
      }
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      progressMoviesMainRecycler.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.progressMoviesTabsViewPadding))
      return
    }
    progressMoviesMainRecycler.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      view.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.progressMoviesTabsViewPadding))
      (progressMoviesEmptyView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarHeight + dimenToPx(R.dimen.spaceBig))
      (progressMoviesOverscrollIcon.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarHeight + dimenToPx(R.dimen.progressMoviesOverscrollIconPadding))
    }
  }

  private fun openSortOrderDialog(order: SortOrder, type: SortType) {
    val options = listOf(NAME, RATING, USER_RATING, NEWEST, DATE_ADDED)
    val args = SortOrderBottomSheet.createBundle(options, order, type)

    requireParentFragment().setFragmentResultListener(REQUEST_SORT_ORDER) { _, bundle ->
      val sortOrder = bundle.getSerializable(ARG_SELECTED_SORT_ORDER) as SortOrder
      val sortType = bundle.getSerializable(ARG_SELECTED_SORT_TYPE) as SortType
      viewModel.setSortOrder(sortOrder, sortType)
    }

    navigateTo(R.id.actionProgressMoviesFragmentToSortOrder, args)
  }

  override fun onEnterSearch() {
    isSearching = true

    progressMoviesMainRecycler.translationY = dimenToPx(R.dimen.progressMoviesSearchLocalOffset).toFloat()
    progressMoviesMainRecycler.smoothScrollToPosition(0)

    overscroll?.detach()
    overscroll = null
  }

  override fun onExitSearch() {
    isSearching = false

    progressMoviesMainRecycler.translationY = 0F
    progressMoviesMainRecycler.smoothScrollToPosition(0)

    setupOverscroll()
  }

  override fun onScrollReset() = progressMoviesMainRecycler.smoothScrollToPosition(0)

  private fun handleEvent(event: Event<*>) {
    when (event) {
      is MovieCheckActionUiEvent -> {
        if (event.isQuickRate) requireMainFragment().openRateDialog(event.movie)
        else parentViewModel.setWatchedMovie(event.movie)
      }
    }
  }

  private fun render(uiState: ProgressMoviesUiState) {
    uiState.run {
      items?.let {
        val resetScroll = scrollReset?.consume() == true
        adapter?.setItems(it, resetScroll)
        progressMoviesEmptyView.fadeIf(items.isEmpty() && !isSearching)
        progressMoviesMainRecycler.fadeIn(withHardware = true).add(animations)
        (requireAppContext() as WidgetsProvider).requestShowsWidgetsUpdate()
      }
      isOverScrollEnabled.let {
        if (it) {
          setupOverscroll()
        } else {
          overscroll?.detach()
          overscroll = null
        }
      }
      sortOrder?.let { event -> event.consume()?.let { openSortOrderDialog(it.first, it.second) } }
    }
  }

  private fun requireMainFragment() = (requireParentFragment() as ProgressMoviesMainFragment)

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    overscroll = null
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
