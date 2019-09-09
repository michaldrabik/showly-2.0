package com.michaldrabik.showly2.ui.search

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
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
import com.michaldrabik.showly2.ui.shows.ShowDetailsFragment.Companion.ARG_SHOW_ID
import com.michaldrabik.showly2.utilities.extensions.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.view_search.*
import kotlin.random.Random

class SearchFragment : BaseFragment<SearchViewModel>() {

  override val layoutResId = R.layout.fragment_search

  private var isInitialized = false

  private lateinit var adapter: SearchAdapter
  private lateinit var layoutManager: LinearLayoutManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    appComponent().inject(this)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(SearchViewModel::class.java)
      .apply {
        uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()
    if (savedInstanceState == null && !isInitialized) isInitialized = true
  }

  private fun setupView() {
    getMainActivity().hideNavigation()
    searchViewInput.visible()
    searchViewText.gone()
    (searchViewIcon.drawable as Animatable).start()
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
    }
  }

  private fun openShowDetails(item: SearchListItem) {
    searchViewLayout.fadeOut()
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
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, item.show.ids.trakt) }
      findNavController().navigate(R.id.actionSearchFragmentToShowDetailsFragment, bundle)
    })
  }

  private fun render(uiModel: SearchUiModel) {
    uiModel.searchItems?.let {
      adapter.setItems(it)
      searchRecycler.scheduleLayoutAnimation()
      searchEmptyView.fadeIf(it.isEmpty())
    }
    uiModel.recentSearchItems?.let { renderRecentSearches(it) }
    uiModel.isSearching?.let {
      if (it) searchEmptyView.gone()
      searchProgress.visibleIf(it)
      searchViewLayout.isEnabled = !it
    }
    uiModel.updateListItem?.let { adapter.updateItem(it) }
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
}
