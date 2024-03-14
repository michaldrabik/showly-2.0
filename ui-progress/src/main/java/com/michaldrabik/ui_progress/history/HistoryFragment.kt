package com.michaldrabik.ui_progress.history

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
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
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.navigateToSafe
import com.michaldrabik.ui_base.utilities.extensions.requireSerializable
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withSpanSizeLookup
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.HistoryPeriod
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.databinding.FragmentHistoryBinding
import com.michaldrabik.ui_progress.helpers.ProgressLayoutManagerProvider
import com.michaldrabik.ui_progress.history.entities.HistoryListItem
import com.michaldrabik.ui_progress.history.filters.HistoryPeriodFilterBottomSheet
import com.michaldrabik.ui_progress.history.filters.HistoryPeriodFilterBottomSheet.Companion.ARG_SELECTED_FILTER
import com.michaldrabik.ui_progress.history.filters.HistoryPeriodFilterBottomSheet.Companion.REQUEST_KEY
import com.michaldrabik.ui_progress.history.recycler.HistoryAdapter
import com.michaldrabik.ui_progress.main.ProgressMainFragment
import com.michaldrabik.ui_progress.main.ProgressMainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class HistoryFragment :
  BaseFragment<HistoryViewModel>(R.layout.fragment_history),
  OnSearchClickListener,
  OnScrollResetListener {

  @Inject lateinit var settings: SettingsViewModeRepository

  override val navigationId = R.id.progressMainFragment
  private val binding by viewBinding(FragmentHistoryBinding::bind)

  override val viewModel by viewModels<HistoryViewModel>()
  private val parentViewModel by viewModels<ProgressMainViewModel>({ requireParentFragment() })

  private var adapter: HistoryAdapter? = null
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
          launch { uiState.collect { viewModel.handleParentAction(it) } }
        }
        with(viewModel) {
          launch { uiState.collect { render(it) } }
        }
      }
    }
  }

  private fun setupRecycler() {
    val gridSpanSize = settings.tabletGridSpanSize
    layoutManager = ProgressLayoutManagerProvider.provideLayoutManger(requireContext(), gridSpanSize)
    (layoutManager as? GridLayoutManager)?.run {
      withSpanSizeLookup { position ->
        when (adapter?.getItems()?.get(position)) {
          is HistoryListItem.Header -> gridSpanSize
          is HistoryListItem.Filters -> gridSpanSize
          is HistoryListItem.Episode -> 1
          else -> throw IllegalStateException()
        }
      }
    }
    adapter = HistoryAdapter(
      onItemClick = { requireMainFragment().openShowDetails(it.show) },
      onDetailsClick = {
        requireMainFragment().openEpisodeDetails(
          show = it.show,
          episode = it.episode,
          season = it.season
        )
      },
      onDatesFilterClick = { openPeriodFilterDialog(it) },
      onImageMissing = { item, force -> viewModel.findMissingImage(item, force) },
      onTranslationMissing = { viewModel.findMissingTranslation(it) },
      listChangeListener = {
        requireMainFragment().resetTranslations()
        layoutManager?.scrollToPosition(0)
      }
    )
    binding.recycler.apply {
      adapter = this@HistoryFragment.adapter
      layoutManager = this@HistoryFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
  }

  private fun setupStatusBar() {
    val recyclerPadding = if (moviesEnabled) {
      R.dimen.progressHistoryTabsViewPadding
    } else {
      R.dimen.progressHistoryTabsViewPaddingNoModes
    }

    if (statusBarHeight != 0) {
      binding.recycler.updatePadding(top = statusBarHeight + dimenToPx(recyclerPadding))
      return
    }

    binding.recycler.doOnApplyWindowInsets { view, insets, _, _ ->
      val tabletOffset = if (isTablet) dimenToPx(R.dimen.spaceMedium) else 0
      statusBarHeight = insets.getInsets(systemBars()).top + tabletOffset
      view.updatePadding(top = statusBarHeight + dimenToPx(recyclerPadding))
    }
  }

  override fun onEnterSearch() {
    isSearching = true

    with(binding) {
      recycler.translationY = dimenToPx(R.dimen.progressSearchLocalOffset).toFloat()
      recycler.smoothScrollToPosition(0)
    }
  }

  override fun onExitSearch() {
    isSearching = false
    with(binding) {
      recycler.translationY = 0F
      recycler.smoothScrollToPosition(0)
    }
  }

  private fun render(uiState: HistoryUiState) {
    with(uiState) {
      with(binding) {
        adapter?.setItems(
          newItems = items,
          notifyChange = resetScrollEvent?.consume() == true
        )
        if (isLoading) {
          recycler.gone()
        } else {
          recycler.fadeIn(150, withHardware = true)
        }
        emptyView.root.visibleIf(!items.any { it is HistoryListItem.Episode } && !isLoading && !isSearching)
        progressBar.visibleIf(isLoading && !isSearching)
      }
    }
  }

  private fun openPeriodFilterDialog(filter: HistoryPeriod) {
    val args = HistoryPeriodFilterBottomSheet.createBundle(filter)
    requireParentFragment().setFragmentResultListener(REQUEST_KEY) { _, bundle ->
      val selected = bundle.requireSerializable<HistoryPeriod>(ARG_SELECTED_FILTER)
      viewModel.setPeriod(selected)
    }
    navigateToSafe(R.id.actionProgressFragmentToDatesFilter, args)
  }

  override fun onScrollReset() = binding.recycler.smoothScrollToPosition(0)

  private fun requireMainFragment() = requireParentFragment() as ProgressMainFragment

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }

  override fun setupBackPressed() = Unit
}
