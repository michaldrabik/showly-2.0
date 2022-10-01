package com.michaldrabik.ui_progress.progress

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
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.EPISODES_LEFT
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.SortOrder.RECENTLY_WATCHED
import com.michaldrabik.ui_model.SortOrder.USER_RATING
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_model.Tip
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SELECTED_NEW_AT_TOP
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SELECTED_SORT_ORDER
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SELECTED_SORT_TYPE
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_SORT_ORDER
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.helpers.TopOverscrollAdapter
import com.michaldrabik.ui_progress.main.EpisodeCheckActionUiEvent
import com.michaldrabik.ui_progress.main.ProgressMainFragment
import com.michaldrabik.ui_progress.main.ProgressMainViewModel
import com.michaldrabik.ui_progress.progress.recycler.ProgressAdapter
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_progress.*
import kotlinx.android.synthetic.main.layout_progress_empty.*
import kotlinx.coroutines.launch
import me.everything.android.ui.overscroll.IOverScrollDecor
import me.everything.android.ui.overscroll.IOverScrollState
import me.everything.android.ui.overscroll.OverScrollBounceEffectDecoratorBase
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator

@AndroidEntryPoint
class ProgressFragment :
  BaseFragment<ProgressViewModel>(R.layout.fragment_progress),
  OnSearchClickListener,
  OnScrollResetListener {

  private companion object {
    const val OVERSCROLL_OFFSET = 250F
    const val OVERSCROLL_OFFSET_TRANSLATION = 4.5F
  }

  private val parentViewModel by viewModels<ProgressMainViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<ProgressViewModel>()

  private var adapter: ProgressAdapter? = null
  private var layoutManager: LinearLayoutManager? = null
  private var overscroll: IOverScrollDecor? = null
  private var statusBarHeight = 0
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
    progressEmptyTraktButton.onClick { requireMainFragment().openTraktSync() }
    progressEmptyDiscoverButton.onClick {
      (requireActivity() as NavigationHost).navigateToDiscover()
    }
    progressTipItem.onClick {
      it.gone()
      showTip(Tip.WATCHLIST_ITEM_PIN)
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = ProgressAdapter(
      itemClickListener = { requireMainFragment().openShowDetails(it.show) },
      itemLongClickListener = { requireMainFragment().openShowMenu(it.show) },
      headerClickListener = { viewModel.toggleHeaderCollapsed(it.type) },
      detailsClickListener = { requireMainFragment().openEpisodeDetails(it.show, it.requireEpisode(), it.requireSeason()) },
      checkClickListener = { viewModel.onEpisodeChecked(it) },
      sortChipClickListener = { viewModel.loadSortOrder() },
      missingImageListener = { item: ProgressListItem, force -> viewModel.findMissingImage(item, force) },
      missingTranslationListener = { viewModel.findMissingTranslation(it) },
      listChangeListener = {
        requireMainFragment().resetTranslations()
        layoutManager?.scrollToPosition(0)
      }
    ).apply {
    }
    progressRecycler.apply {
      adapter = this@ProgressFragment.adapter
      layoutManager = this@ProgressFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
  }

  private fun setupStatusBar() {
    val recyclerPadding = if (moviesEnabled) R.dimen.progressTabsViewPadding else R.dimen.progressTabsViewPaddingNoModes
    val overscrollPadding = if (moviesEnabled) R.dimen.progressOverscrollIconPadding else R.dimen.progressOverscrollIconPaddingNoModes
    if (statusBarHeight != 0) {
      progressRecycler.updatePadding(top = statusBarHeight + dimenToPx(recyclerPadding))
      (progressOverscrollIcon.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarHeight + dimenToPx(overscrollPadding))
      return
    }
    progressRecycler.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      view.updatePadding(top = statusBarHeight + dimenToPx(recyclerPadding))
      (progressEmptyView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarHeight + dimenToPx(R.dimen.spaceBig))
      (progressOverscrollIcon.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarHeight + dimenToPx(overscrollPadding))
    }
  }

  private fun setupOverscroll() {
    if (overscroll != null) return
    val adapt = TopOverscrollAdapter(progressRecycler)
    overscroll = VerticalOverScrollBounceEffectDecorator(
      adapt,
      1.75F,
      OverScrollBounceEffectDecoratorBase.DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK,
      OverScrollBounceEffectDecoratorBase.DEFAULT_DECELERATE_FACTOR
    ).apply {
      setOverScrollUpdateListener { _, state, offset ->
        progressOverscrollIcon?.run {
          if (offset > 0) {
            val value = (offset / OVERSCROLL_OFFSET).coerceAtMost(1F)
            val valueTranslation = offset / OVERSCROLL_OFFSET_TRANSLATION
            when (state) {
              IOverScrollState.STATE_DRAG_START_SIDE -> {
                alpha = value
                scaleX = value
                scaleY = value
                translationY = valueTranslation
                overscrollEnabled = true
              }
              IOverScrollState.STATE_BOUNCE_BACK -> {
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

  private fun openSortOrderDialog(order: SortOrder, type: SortType, newAtTop: Boolean) {
    val options = listOf(NAME, RATING, USER_RATING, NEWEST, RECENTLY_WATCHED, EPISODES_LEFT)
    val args = SortOrderBottomSheet.createBundle(options, order, type, newAtTop = Pair(true, newAtTop))

    requireParentFragment().setFragmentResultListener(REQUEST_SORT_ORDER) { _, bundle ->
      val sortOrder = bundle.getSerializable(ARG_SELECTED_SORT_ORDER) as SortOrder
      val sortType = bundle.getSerializable(ARG_SELECTED_SORT_TYPE) as SortType
      val newTop = bundle.getBoolean(ARG_SELECTED_NEW_AT_TOP)
      viewModel.setSortOrder(sortOrder, sortType, newTop)
    }

    navigateTo(R.id.actionProgressFragmentToSortOrder, args)
  }

  override fun onEnterSearch() {
    isSearching = true

    progressRecycler.translationY = dimenToPx(R.dimen.progressSearchLocalOffset).toFloat()
    progressRecycler.smoothScrollToPosition(0)

    overscroll?.detach()
    overscroll = null
  }

  override fun onExitSearch() {
    isSearching = false

    progressRecycler.translationY = 0F
    progressRecycler.smoothScrollToPosition(0)

    setupOverscroll()
  }

  private fun handleEvent(event: Event<*>) {
    when (event) {
      is EpisodeCheckActionUiEvent -> {
        if (event.isQuickRate) requireMainFragment().openRateDialog(event.episode)
        else parentViewModel.setWatchedEpisode(event.episode)
      }
    }
  }

  private fun render(uiState: ProgressUiState) {
    uiState.run {
      items?.let {
        val resetScroll = scrollReset?.consume() == true
        adapter?.setItems(it, resetScroll)
        progressEmptyView.visibleIf(it.isEmpty() && !isLoading && !isSearching)
        progressTipItem.visibleIf(it.count() >= 3 && !isTipShown(Tip.WATCHLIST_ITEM_PIN))
        progressRecycler.fadeIn(withHardware = true).add(animations)
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
      sortOrder?.let { event ->
        event.consume()?.let {
          openSortOrderDialog(it.first, it.second, it.third)
        }
      }
    }
  }

  override fun onScrollReset() = progressRecycler.smoothScrollToPosition(0)

  private fun requireMainFragment() = requireParentFragment() as ProgressMainFragment

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    overscroll = null
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
