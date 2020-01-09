package com.michaldrabik.showly2.ui.search

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.model.RecentSearch
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.search.recycler.SearchAdapter
import com.michaldrabik.showly2.ui.search.recycler.SearchListItem
import com.michaldrabik.showly2.ui.search.views.RecentSearchView
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment.Companion.ARG_SHOW_ID
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.fadeIf
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.hideKeyboard
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.shake
import com.michaldrabik.showly2.utilities.extensions.showKeyboard
import com.michaldrabik.showly2.utilities.extensions.visible
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.view_search.*
import kotlin.random.Random

class SearchFragment : BaseFragment<SearchViewModel>() {

  override val layoutResId = R.layout.fragment_search

  private var isInitialized = false

  private lateinit var adapter: SearchAdapter
  private lateinit var layoutManager: LinearLayoutManager

  private val swipeRefreshEndOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshEndOffset) }
  private val swipeRefreshStartOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshStartOffset) }

  override fun getSnackbarHost(): ViewGroup = searchRoot

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun createViewModel(provider: ViewModelProvider) =
    provider.get(SearchViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()
    if (savedInstanceState == null && !isInitialized) isInitialized = true
    viewModel.run {
      uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      errorStream.observe(viewLifecycleOwner, Observer { showErrorSnackbar(it!!) })
    }
  }

  override fun onResume() {
    super.onResume()
    handleBackPressed()
  }

  private fun setupView() {
    hideNavigation()
    searchViewInput.visible()
    searchViewText.gone()
    (searchViewIcon.drawable as Animatable).start()
    searchViewLayout.settingsIconVisible = false
    viewModel.loadLastSearch()
    if (!isInitialized) {
      searchViewInput.showKeyboard()
      searchViewInput.requestFocus()
      viewModel.loadRecentSearches()
    }

    searchViewInput.setOnEditorActionListener { textView, id, _ ->
      if (id == EditorInfo.IME_ACTION_SEARCH) {
        val query = textView.text.toString()
        if (query.trim().isBlank()) {
          searchViewLayout.shake()
          return@setOnEditorActionListener true
        }
        viewModel.searchForShow(query)
        searchViewInput.hideKeyboard()
        searchViewInput.clearFocus()
      }
      true
    }

    searchViewIcon.onClick {
      searchViewInput.hideKeyboard()
      requireActivity().onBackPressed()
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(requireContext(), VERTICAL, false)
    adapter = SearchAdapter()
    adapter.missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
    adapter.itemClickListener = { openShowDetails(it) }
    searchRecycler.apply {
      setHasFixedSize(true)
      adapter = this@SearchFragment.adapter
      layoutManager = this@SearchFragment.layoutManager
      itemAnimator = null
    }

    searchSwipeRefresh.apply {
      isEnabled = false
      setProgressBackgroundColorSchemeResource(R.color.colorSearchViewBackground)
      setColorSchemeResources(R.color.colorAccent, R.color.colorAccent, R.color.colorAccent)
      setProgressViewOffset(false, swipeRefreshStartOffset, swipeRefreshEndOffset)
    }
  }

  private fun openShowDetails(item: SearchListItem) {
    val clickedIndex = adapter.indexOf(item)
    (0..adapter.itemCount).forEach {
      if (it != clickedIndex) {
        val view = searchRecycler.findViewHolderForAdapterPosition(it)
        view?.let { v ->
          val randomDelay = Random.nextLong(50, 200)
          v.itemView.fadeOut(duration = 150, startDelay = randomDelay)
        }
      }
    }
    val clickedView = searchRecycler.findViewHolderForAdapterPosition(clickedIndex)
    clickedView?.itemView?.fadeOut(duration = 150, startDelay = 350, endAction = {
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, item.show.ids.trakt.id) }
      findNavController().navigate(R.id.actionSearchFragmentToShowDetailsFragment, bundle)
    })
  }

  private fun render(uiModel: SearchUiModel) {
    uiModel.run {
      searchItems?.let {
        adapter.setItems(it)
        if (searchItemsAnimate == true) searchRecycler.scheduleLayoutAnimation()
      }
      recentSearchItems?.let { renderRecentSearches(it) }
      isSearching?.let {
        searchSwipeRefresh.isRefreshing = it
        searchViewLayout.isEnabled = !it
      }
      isEmpty?.let { searchEmptyView.fadeIf(it) }
      isInitial?.let { searchInitialView.fadeIf(it) }
    }
  }

  private fun renderRecentSearches(it: List<RecentSearch>) {
    if (it.isEmpty()) {
      searchRecentsClearButton.fadeOut()
      searchRecentsLayout.fadeOut {
        searchRecentsLayout.removeAllViews()
      }
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
          searchViewInput.setText(item.text)
          viewModel.searchForShow(item.text)
        }
      }
      searchRecentsLayout.addView(view)
    }
  }

  private fun handleBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      showNavigation()
      remove()
      findNavController().popBackStack()
    }
  }
}
