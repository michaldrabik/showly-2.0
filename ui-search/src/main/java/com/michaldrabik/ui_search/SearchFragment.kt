package com.michaldrabik.ui_search

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.sheets.context_menu.ContextMenuBottomSheet
import com.michaldrabik.ui_base.common.sheets.sort_order.SortOrderBottomSheet
import com.michaldrabik.ui_base.common.views.exSearchViewIcon
import com.michaldrabik.ui_base.common.views.exSearchViewInput
import com.michaldrabik.ui_base.common.views.exSearchViewText
import com.michaldrabik.ui_base.utilities.extensions.add
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
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
import com.michaldrabik.ui_base.utilities.extensions.navigateToSafe
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.shake
import com.michaldrabik.ui_base.utilities.extensions.showKeyboard
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.RecentSearch
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SELECTED_SORT_ORDER
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SELECTED_SORT_TYPE
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_SORT_ORDER
import com.michaldrabik.ui_search.recycler.SearchAdapter
import com.michaldrabik.ui_search.recycler.SearchListItem
import com.michaldrabik.ui_search.recycler.suggestions.SuggestionAdapter
import com.michaldrabik.ui_search.utilities.TextWatcherAdapter
import com.michaldrabik.ui_search.views.RecentSearchView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_search.*

@AndroidEntryPoint
class SearchFragment : BaseFragment<SearchViewModel>(R.layout.fragment_search), TextWatcherAdapter {

  companion object {
    private const val ARG_HEADER_TRANSLATION = "ARG_HEADER_TRANSLATION"
  }

  override val viewModel by viewModels<SearchViewModel>()
  override val navigationId = R.id.searchFragment

  private var adapter: SearchAdapter? = null
  private var suggestionsAdapter: SuggestionAdapter? = null
  private var layoutManager: LinearLayoutManager? = null
  private var suggestionsLayoutManager: LinearLayoutManager? = null

  private val swipeRefreshEndOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshEndOffset) }
  private val swipeRefreshStartOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshStartOffset) }

  private var headerTranslation = 0F

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    savedInstanceState?.let {
      headerTranslation = it.getFloat(ARG_HEADER_TRANSLATION)
    }
    return super.onCreateView(inflater, container, savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    enableUi()
    setupView()
    setupRecycler()
    setupSuggestionsRecycler()
    setupStatusBar()

    if (savedInstanceState == null && !isInitialized) {
      isInitialized = true
    }

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.messageFlow.collect { showSnack(it) } }
    )
  }

  override fun onPause() {
    enableUi()
    headerTranslation = searchFiltersView.translationY
    super.onPause()
  }

  override fun onStop() {
    viewModel.clearSuggestions()
    exSearchViewInput.removeTextChangedListener(this)
    exSearchViewInput.setText("")
    super.onStop()
  }

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }

  private fun setupView() {
    exSearchViewInput.visible()
    exSearchViewText.gone()
    (exSearchViewIcon.drawable as Animatable).start()
    searchViewLayout.settingsIconVisible = false
    viewModel.preloadSuggestions()
    if (!isInitialized) {
      exSearchViewInput.showKeyboard()
      exSearchViewInput.requestFocus()
      viewModel.loadRecentSearches()
    }

    exSearchViewInput.run {
      addTextChangedListener(this@SearchFragment)
      setOnEditorActionListener { textView, id, _ ->
        if (id == EditorInfo.IME_ACTION_SEARCH) {
          val query = textView.text.toString()
          if (query.trim().isBlank()) {
            searchViewLayout?.shake()
            return@setOnEditorActionListener true
          }
          viewModel.search(query)
          exSearchViewInput.hideKeyboard()
          exSearchViewInput.clearFocus()
        }
        true
      }
    }
    exSearchViewIcon.onClick {
      exSearchViewInput.hideKeyboard()
      requireActivity().onBackPressed()
    }
    with(searchFiltersView) {
      onChipsChangeListener = viewModel::setFilters
      onSortClickListener = ::openSortingDialog
      translationY = headerTranslation
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(requireContext(), VERTICAL, false)
    adapter = SearchAdapter(
      itemClickListener = { openShowDetails(it) },
      itemLongClickListener = { openContextMenu(it) },
      missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) },
      listChangeListener = { searchRecycler.scrollToPosition(0) }
    )
    searchRecycler.apply {
      setHasFixedSize(true)
      adapter = this@SearchFragment.adapter
      layoutManager = this@SearchFragment.layoutManager
      itemAnimator = null
      clearOnScrollListeners()
      addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
          val value = searchFiltersView.translationY - dy
          searchFiltersView.translationY = value.coerceAtMost(0F)
        }
      })
    }

    searchSwipeRefresh.apply {
      isEnabled = false
      val color = requireContext().colorFromAttr(R.attr.colorAccent)
      setProgressBackgroundColorSchemeColor(requireContext().colorFromAttr(R.attr.colorSearchViewBackground))
      setColorSchemeColors(color, color, color)
      setProgressViewOffset(false, swipeRefreshStartOffset, swipeRefreshEndOffset)
    }
  }

  private fun setupSuggestionsRecycler() {
    suggestionsLayoutManager = LinearLayoutManager(requireContext(), VERTICAL, false)
    suggestionsAdapter = SuggestionAdapter(
      itemClickListener = {
        val query =
          if (it.translation?.title?.isNotBlank() == true) it.translation.title
          else it.title
        viewModel.saveRecentSearch(query)
        openDetails(it)
      },
      missingImageListener = { ids, force -> viewModel.loadMissingSuggestionImage(ids, force) },
      missingTranslationListener = { viewModel.loadMissingSuggestionTranslation(it) }
    )
    suggestionsRecycler.apply {
      adapter = this@SearchFragment.suggestionsAdapter
      layoutManager = this@SearchFragment.suggestionsLayoutManager
      itemAnimator = null
    }
  }

  private fun setupStatusBar() {
    searchRoot.doOnApplyWindowInsets { view, insets, _, _ ->
      val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      view.updatePadding(top = inset)
    }
  }

  private fun openSortingDialog(order: SortOrder, type: SortType) {
    val options = listOf(SortOrder.RANK, SortOrder.NAME, SortOrder.NEWEST)
    val args = SortOrderBottomSheet.createBundle(options, order, type)

    setFragmentResultListener(REQUEST_SORT_ORDER) { _, bundle ->
      val sortOrder = bundle.getSerializable(ARG_SELECTED_SORT_ORDER) as SortOrder
      val sortType = bundle.getSerializable(ARG_SELECTED_SORT_TYPE) as SortType
      viewModel.setSortOrder(sortOrder, sortType)
    }

    navigateToSafe(R.id.actionSearchFragmentToSortOrder, args)
  }

  private fun openShowDetails(item: SearchListItem) {
    disableUi()
    searchRoot?.fadeOut(150) {
      openDetails(item)
    }.add(animations)
  }

  private fun openDetails(item: SearchListItem) {
    if (item.isShow) {
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, item.show.traktId) }
      navigateToSafe(R.id.actionSearchFragmentToShowDetailsFragment, bundle)
    } else if (item.isMovie) {
      val bundle = Bundle().apply { putLong(ARG_MOVIE_ID, item.movie.traktId) }
      navigateToSafe(R.id.actionSearchFragmentToMovieDetailsFragment, bundle)
    }
  }

  private fun openContextMenu(item: SearchListItem) {
    setFragmentResultListener(NavigationArgs.REQUEST_ITEM_MENU) { requestKey, _ ->
      if (requestKey == NavigationArgs.REQUEST_ITEM_MENU) {
        viewModel.refreshFollowState(item)
      }
      clearFragmentResultListener(NavigationArgs.REQUEST_ITEM_MENU)
    }
    if (item.isShow) {
      val bundle = ContextMenuBottomSheet.createBundle(item.show.ids.trakt)
      navigateToSafe(R.id.actionSearchFragmentToShowItemMenu, bundle)
    } else if (item.isMovie) {
      val bundle = ContextMenuBottomSheet.createBundle(item.movie.ids.trakt)
      navigateToSafe(R.id.actionSearchFragmentToMovieItemMenu, bundle)
    }
  }

  private fun render(uiState: SearchUiState) {
    uiState.run {
      searchItems?.let {
        val resetScroll = resetScroll?.consume() == true
        adapter?.setItems(it, resetScroll)
        if (searchItemsAnimate?.consume() == true) {
          searchRecycler.scheduleLayoutAnimation()
        }
        if (resetScroll) {
          searchFiltersView.translationY = 0F
        }
      }
      recentSearchItems?.let { renderRecentSearches(it) }
      suggestionsItems?.let {
        suggestionsAdapter?.setItems(it)
        suggestionsRecycler.visibleIf(it.isNotEmpty())
      }
      searchOptions?.let {
        searchFiltersView.setTypes(it.filters)
        searchFiltersView.setSorting(it.sortOrder, it.sortType)
      }
      isSearching.let {
        searchSwipeRefresh.isRefreshing = it
        searchViewLayout.isEnabled = !it
      }
      sortOrder?.let { event ->
        event.consume()?.let { openSortingDialog(it.first, it.second) }
      }
      isMoviesEnabled.let { isEnabled ->
        val types = mutableListOf(Mode.SHOWS).apply {
          if (isEnabled) add(Mode.MOVIES)
        }
        searchFiltersView.setEnabledTypes(types)
      }
      searchEmptyView.fadeIf(isEmpty)
      searchInitialView.fadeIf(isInitial)
      searchFiltersView.visibleIf(isFiltersVisible)
    }
  }

  private fun renderRecentSearches(it: List<RecentSearch>) {
    if (it.isEmpty()) {
      searchRecentsClearButton.gone()
      searchRecentsLayout.removeAllViews()
      searchRecentsLayout.gone()
      return
    }

    searchRecentsLayout.fadeIn()
    searchRecentsClearButton.fadeIn()
    searchRecentsClearButton.onClick { viewModel.clearRecentSearches() }

    val paddingH = requireContext().dimenToPx(R.dimen.searchViewItemPaddingHorizontal)
    val paddingV = requireContext().dimenToPx(R.dimen.spaceMedium)

    searchRecentsLayout.removeAllViews()
    it.forEach { item ->
      val view = RecentSearchView(requireContext()).apply {
        setPadding(paddingH, paddingV, paddingH, paddingV)
        bind(item)
        onClick {
          viewModel.search(item.text)
          exSearchViewInput.setText(item.text)
        }
      }
      searchRecentsLayout.addView(view)
    }
  }

  override fun afterTextChanged(text: Editable?) {
    viewModel.loadSuggestions(text.toString())
  }
}
