package com.michaldrabik.ui_search

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.michaldrabik.ui_base.BaseFragment
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
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.shake
import com.michaldrabik.ui_base.utilities.extensions.showKeyboard
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.RecentSearch
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_search.recycler.SearchAdapter
import com.michaldrabik.ui_search.recycler.SearchListItem
import com.michaldrabik.ui_search.recycler.suggestions.SuggestionAdapter
import com.michaldrabik.ui_search.utilities.TextWatcherAdapter
import com.michaldrabik.ui_search.views.RecentSearchView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : BaseFragment<SearchViewModel>(R.layout.fragment_search), TextWatcherAdapter {

  override val viewModel by viewModels<SearchViewModel>()

  private var adapter: SearchAdapter? = null
  private var suggestionsAdapter: SuggestionAdapter? = null
  private var layoutManager: LinearLayoutManager? = null
  private var suggestionsLayoutManager: LinearLayoutManager? = null

  private val swipeRefreshEndOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshEndOffset) }
  private val swipeRefreshStartOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshStartOffset) }

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

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          launch { messageState.collect { showSnack(it) } }
        }
      }
    }
  }

  override fun onPause() {
    enableUi()
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
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(requireContext(), VERTICAL, false)
    adapter = SearchAdapter().apply {
      itemClickListener = { openShowDetails(it) }
      missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
    }
    searchRecycler.apply {
      setHasFixedSize(true)
      adapter = this@SearchFragment.adapter
      layoutManager = this@SearchFragment.layoutManager
      itemAnimator = null
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
    suggestionsAdapter = SuggestionAdapter().apply {
      itemClickListener = {
        val query =
          if (it.translation?.title?.isNotBlank() == true) it.translation.title
          else it.title
        viewModel.saveRecentSearch(query)
        openDetails(it)
      }
      missingImageListener = { ids, force -> viewModel.loadMissingSuggestionImage(ids, force) }
      missingTranslationListener = { viewModel.loadMissingSuggestionTranslation(it) }
    }
    suggestionsRecycler.apply {
      adapter = this@SearchFragment.suggestionsAdapter
      layoutManager = this@SearchFragment.suggestionsLayoutManager
      itemAnimator = null
    }
  }

  private fun setupStatusBar() {
    searchRoot.doOnApplyWindowInsets { view, insets, _, _ ->
      view.updatePadding(top = insets.systemWindowInsetTop)
    }
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
      navigateTo(R.id.actionSearchFragmentToShowDetailsFragment, bundle)
    } else if (item.isMovie) {
      val bundle = Bundle().apply { putLong(ARG_MOVIE_ID, item.movie.traktId) }
      navigateTo(R.id.actionSearchFragmentToMovieDetailsFragment, bundle)
    }
  }

  private fun render(uiState: SearchUiState) {
    uiState.run {
      searchItems?.let {
        adapter?.setItems(it)
        if (searchItemsAnimate?.consume() == true) {
          searchRecycler.scheduleLayoutAnimation()
        }
      }
      recentSearchItems?.let { renderRecentSearches(it) }
      suggestionsItems?.let {
        suggestionsAdapter?.setItems(it)
        suggestionsRecycler.visibleIf(it.isNotEmpty())
      }
      isSearching.let {
        searchSwipeRefresh.isRefreshing = it
        searchViewLayout.isEnabled = !it
      }
      searchEmptyView.fadeIf(isEmpty)
      searchInitialView.fadeIf(isInitial)
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
