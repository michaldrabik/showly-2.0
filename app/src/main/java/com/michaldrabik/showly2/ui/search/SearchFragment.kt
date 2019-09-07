package com.michaldrabik.showly2.ui.search

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.search.recycler.SearchAdapter
import com.michaldrabik.showly2.utilities.extensions.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.view_search.*

class SearchFragment : BaseFragment<SearchViewModel>() {

  override val layoutResId = R.layout.fragment_search

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
  }

  private fun setupView() {
    (searchViewIcon.drawable as Animatable).start()
    searchViewInput.visible()
    searchViewText.gone()
    searchViewInput.showKeyboard()
    searchViewInput.requestFocus()

    searchViewInput.setOnEditorActionListener { textView, id, _ ->
      if (id == EditorInfo.IME_ACTION_SEARCH) {
        viewModel.searchForShow(textView.text.toString())
        searchViewInput.hideKeyboard()
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
    adapter.itemClickListener = { }
    searchRecycler.apply {
      setHasFixedSize(true)
      adapter = this@SearchFragment.adapter
      layoutManager = this@SearchFragment.layoutManager
//      addItemDecoration(DividerItemDecoration(requireContext(), VERTICAL))
    }
  }

  private fun render(uiModel: SearchUiModel) {
    uiModel.searchItems?.let {
      adapter.setItems(it)
      searchRecycler.scheduleLayoutAnimation()
    }
    uiModel.isSearching?.let {
      searchProgress.visibleIf(it)
      searchViewLayout.isEnabled = !it
    }
    uiModel.updateListItem?.let { adapter.updateItem(it) }
  }
}
