package com.michaldrabik.ui_lists.lists

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.postDelayed
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.repository.settings.SettingsViewModeRepository
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.common.sheets.sort_order.SortOrderBottomSheet
import com.michaldrabik.ui_base.events.Event
import com.michaldrabik.ui_base.events.EventsManager
import com.michaldrabik.ui_base.events.TraktListQuickSyncSuccess
import com.michaldrabik.ui_base.events.TraktQuickSyncSuccess
import com.michaldrabik.ui_base.utilities.ModeHost
import com.michaldrabik.ui_base.utilities.extensions.add
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.disableUi
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.enableUi
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.hideKeyboard
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showKeyboard
import com.michaldrabik.ui_base.utilities.extensions.updateTopMargin
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.databinding.FragmentListsBinding
import com.michaldrabik.ui_lists.lists.helpers.ListsLayoutManagerProvider
import com.michaldrabik.ui_lists.lists.recycler.ListsAdapter
import com.michaldrabik.ui_lists.lists.recycler.ListsItem
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_UPDATED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_LIST
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_CREATE_LIST
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ListsFragment :
  BaseFragment<ListsViewModel>(R.layout.fragment_lists),
  OnTabReselectedListener {

  companion object {
    private const val TRANSLATION_DURATION = 225L
  }

  override val viewModel by viewModels<ListsViewModel>()
  private val binding by viewBinding(FragmentListsBinding::bind)

  @Inject lateinit var eventsManager: EventsManager
  @Inject lateinit var settings: SettingsViewModeRepository

  private var adapter: ListsAdapter? = null
  private var layoutManager: LayoutManager? = null

  private var searchViewTranslation = 0F
  private var tabsTranslation = 0F
  private var isFabHidden = false
  private var isSearching = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    savedInstanceState?.let {
      searchViewTranslation = it.getFloat("ARG_SEARCH_POSITION")
      tabsTranslation = it.getFloat("ARG_TABS_POSITION")
      isFabHidden = it.getBoolean("ARG_FAB_HIDDEN")
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupStatusBar()
    setupRecycler()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { eventsManager.events.collect { handleEvent(it) } },
      doAfterLaunch = { viewModel.loadItems(resetScroll = false) }
    )
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putFloat("ARG_SEARCH_POSITION", searchViewTranslation)
    outState.putFloat("ARG_TABS_POSITION", tabsTranslation)
    outState.putBoolean("ARG_FAB_HIDDEN", isFabHidden)
  }

  override fun onPause() {
    enableUi()
    with(binding) {
      tabsTranslation = fragmentListsModeTabs.translationY
      searchViewTranslation = fragmentListsSearchView.translationY
      isFabHidden = fragmentListsCreateListButton.visibility != VISIBLE
    }
    super.onPause()
  }

  private fun setupView() {
    with(binding) {
      fragmentListsSearchView.run {
        hint = getString(R.string.textSearchFor)
        onSettingsClickListener = { openSettings() }
      }
      with(fragmentListsSearchLocalView) {
        onCloseClickListener = { exitSearch() }
      }
      fragmentListsModeTabs.run {
        onModeSelected = { (requireActivity() as ModeHost).setMode(it, force = true) }
        showMovies(moviesEnabled)
        showLists(true, anchorEnd = moviesEnabled)
        selectLists()
      }
      fragmentListsCreateListButton.run {
        if (!isFabHidden) fadeIn()
        onClick { openCreateList() }
      }
      fragmentListsFilters.onSortClickListener = { sortOrder, sortType ->
        showSortOrderDialog(sortOrder, sortType)
      }
      fragmentListsSearchButton.run {
        onClick { if (!isSearching) enterSearch() else exitSearch() }
      }
      fragmentListsSearchView.onClick { openMainSearch() }

      fragmentListsSearchView.translationY = searchViewTranslation
      fragmentListsModeTabs.translationY = tabsTranslation
      fragmentListsIcons.translationY = tabsTranslation
    }
  }

  private fun setupStatusBar() {
    with(binding) {
      fragmentListsRoot.doOnApplyWindowInsets { _, insets, _, _ ->
        val tabletOffset = if (isTablet) dimenToPx(R.dimen.spaceMedium) else 0
        val statusBarSize = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top + tabletOffset
        fragmentListsRecycler
          .updatePadding(top = statusBarSize + dimenToPx(R.dimen.listsRecyclerPaddingTop))
        fragmentListsSearchView.applyWindowInsetBehaviour(dimenToPx(R.dimen.spaceNormal) + statusBarSize)
        fragmentListsSearchView.updateTopMargin(dimenToPx(R.dimen.spaceMedium) + statusBarSize)
        fragmentListsModeTabs.updateTopMargin(dimenToPx(R.dimen.collectionTabsMargin) + statusBarSize)
        fragmentListsIcons.updateTopMargin(dimenToPx(R.dimen.listsIconsPadding) + statusBarSize)
        fragmentListsSearchLocalView.updateTopMargin(dimenToPx(R.dimen.listsSearchLocalViewPadding) + statusBarSize)
        fragmentListsEmptyView.root.updateTopMargin(statusBarSize)
      }
    }
  }

  private fun setupRecycler() {
    layoutManager = ListsLayoutManagerProvider.provideLayoutManger(requireContext(), settings)
    adapter = ListsAdapter().apply {
      stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
      itemClickListener = { openListDetails(it) }
      itemsChangedListener = {
        resetTranslations()
        layoutManager?.scrollToPosition(0)
      }
      missingImageListener = { item, itemImage, force ->
        viewModel.loadMissingImage(item, itemImage, force)
      }
    }
    with(binding) {
      fragmentListsRecycler.apply {
        adapter = this@ListsFragment.adapter
        layoutManager = this@ListsFragment.layoutManager
        (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        setHasFixedSize(true)
        clearOnScrollListeners()
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
          var isFading = false
          override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (isFading) return
            val position = (this@ListsFragment.layoutManager as? LinearLayoutManager)?.findFirstCompletelyVisibleItemPosition()
              ?: (this@ListsFragment.layoutManager as? GridLayoutManager)?.findFirstCompletelyVisibleItemPosition()
            if ((position ?: 0) > 1) {
              if (fragmentListsCreateListButton.visibility != VISIBLE) return
              fragmentListsCreateListButton
                .fadeOut(125, endAction = { isFading = false })
                .add(animations)
            } else {
              if (fragmentListsCreateListButton.visibility != GONE) return
              fragmentListsCreateListButton
                .fadeIn(125, endAction = { isFading = false })
                .add(animations)
            }
            isFading = true
          }
        })
      }
    }
  }

  override fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      if (isSearching) {
        exitSearch()
      } else {
        isEnabled = false
        activity?.onBackPressed()
      }
    }
  }

  private fun enterSearch() {
    resetTranslations()
    with(binding) {
      fragmentListsSearchLocalView.fadeIn(150)
      fragmentListsIcons.gone()
      fragmentListsRecycler.smoothScrollToPosition(0)
      with(fragmentListsSearchLocalView.binding.searchViewLocalInput) {
        setText("")
        doAfterTextChanged {
          viewModel.loadItems(
            searchQuery = it.toString().trim(),
            resetScroll = true
          )
        }
        visible()
        showKeyboard()
        requestFocus()
      }
    }
    isSearching = true
  }

  private fun exitSearch() {
    with(binding) {
      isSearching = false
      resetTranslations()
      fragmentListsSearchLocalView.gone()
      fragmentListsIcons.visible()
      fragmentListsRecycler.translationY = 0F
      fragmentListsRecycler.postDelayed(200) { layoutManager?.scrollToPosition(0) }
      with(fragmentListsSearchLocalView.binding.searchViewLocalInput) {
        setText("")
        gone()
        hideKeyboard()
        clearFocus()
      }
    }
  }

  private fun showSortOrderDialog(sortOrder: SortOrder, sortType: SortType) {
    val options = listOf(NAME, NEWEST, DATE_UPDATED)
    val args = SortOrderBottomSheet.createBundle(options, sortOrder, sortType)

    setFragmentResultListener(NavigationArgs.REQUEST_SORT_ORDER) { _, bundle ->
      val order = bundle.getSerializable(NavigationArgs.ARG_SELECTED_SORT_ORDER) as SortOrder
      val type = bundle.getSerializable(NavigationArgs.ARG_SELECTED_SORT_TYPE) as SortType
      viewModel.setSortOrder(order, type)
    }

    navigateTo(R.id.actionListsFragmentToSortOrder, args)
  }

  private fun render(uiState: ListsUiState) {
    uiState.run {
      with(binding) {
        items?.let {
          fragmentListsEmptyView.root.fadeIf(it.isEmpty() && !isSearching)
          fragmentListsSearchButton.visibleIf(it.isNotEmpty() || isSearching)

          val resetScroll = resetScroll.consume() == true
          adapter?.setItems(it, resetScroll)
        }
        sortOrder?.let {
          fragmentListsFilters.setSorting(it.first, it.second)
        }
        isSyncing?.let {
          fragmentListsSearchView.setTraktProgress(it)
          fragmentListsSearchView.isEnabled = !it
        }
      }
    }
  }

  private fun openMainSearch() {
    disableUi()
    hideNavigation()
    with(binding) {
      fragmentListsModeTabs.fadeOut(duration = 200).add(animations)
      fragmentListsIcons.fadeOut(duration = 200).add(animations)
      fragmentListsRecycler.fadeOut(duration = 200) {
        super.navigateTo(R.id.actionListsFragmentToSearch, null)
      }.add(animations)
    }
  }

  private fun openListDetails(listItem: ListsItem) {
    disableUi()
    hideNavigation()
    binding.fragmentListsRoot.fadeOut(150) {
      val bundle = bundleOf(ARG_LIST to listItem.list)
      navigateTo(R.id.actionListsFragmentToDetailsFragment, bundle)
      exitSearch()
    }.add(animations)
  }

  private fun openSettings() {
    hideNavigation()
    exitSearch()
    navigateTo(R.id.actionListsFragmentToSettingsFragment)
  }

  private fun openCreateList() {
    setFragmentResultListener(REQUEST_CREATE_LIST) { _, _ -> viewModel.loadItems(resetScroll = true) }
    navigateTo(R.id.actionListsFragmentToCreateListDialog, bundleOf())
  }

  private fun resetTranslations(duration: Long = TRANSLATION_DURATION) {
    if (view == null) return
    with(binding) {
      arrayOf(
        fragmentListsSearchView,
        fragmentListsModeTabs,
        fragmentListsIcons,
        fragmentListsSearchLocalView
      ).forEach {
        it.animate().translationY(0F).setDuration(duration).add(animations)?.start()
      }
    }
  }

  private fun handleEvent(event: Event) {
    when (event) {
      is TraktListQuickSyncSuccess -> {
        val text = resources.getQuantityString(R.plurals.textTraktQuickSyncComplete, 1, 1)
        binding.fragmentListsSnackHost.showInfoSnackbar(text)
      }

      is TraktQuickSyncSuccess -> {
        val text = resources.getQuantityString(R.plurals.textTraktQuickSyncComplete, event.count, event.count)
        binding.fragmentListsSnackHost.showInfoSnackbar(text)
      }

      else -> Unit
    }
  }

  override fun onTabReselected() {
    if (view == null) return
    resetTranslations()
    binding.fragmentListsRecycler.smoothScrollToPosition(0)
  }

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
