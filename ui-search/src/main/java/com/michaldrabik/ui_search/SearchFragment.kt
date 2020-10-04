package com.michaldrabik.ui_search

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.michaldrabik.showly2.ui.search.recycler.SearchAdapter
import com.michaldrabik.showly2.ui.search.recycler.SearchListItem
import com.michaldrabik.showly2.ui.search.views.RecentSearchView
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.views.exSearchViewIcon
import com.michaldrabik.ui_base.common.views.exSearchViewInput
import com.michaldrabik.ui_base.common.views.exSearchViewText
import com.michaldrabik.ui_base.utilities.extensions.*
import com.michaldrabik.ui_model.RecentSearch
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_search.di.UiSearchComponentProvider
import kotlinx.android.synthetic.main.fragment_search.*
import kotlin.random.Random

class SearchFragment : BaseFragment<SearchViewModel>(R.layout.fragment_search) {

  override val viewModel by viewModels<SearchViewModel> { viewModelFactory }

  private lateinit var adapter: SearchAdapter
  private lateinit var layoutManager: LinearLayoutManager

  private val swipeRefreshEndOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshEndOffset) }
  private val swipeRefreshStartOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshStartOffset) }

  override fun getSnackbarHost(): ViewGroup = searchRoot

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiSearchComponentProvider).provideSearchComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()
    setupStatusBar()

    if (savedInstanceState == null && !isInitialized) {
      isInitialized = true
    }

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
      messageLiveData.observe(viewLifecycleOwner, { showSnack(it) })
    }
  }

  override fun onResume() {
    super.onResume()
    handleBackPressed()
  }

  private fun setupView() {
    hideNavigation()
    exSearchViewInput.visible()
    exSearchViewText.gone()
    (exSearchViewIcon.drawable as Animatable).start()
    searchViewLayout.settingsIconVisible = false
    viewModel.loadLastSearch()
    if (!isInitialized) {
      exSearchViewInput.showKeyboard()
      exSearchViewInput.requestFocus()
      viewModel.loadRecentSearches()
    }

    exSearchViewInput.setOnEditorActionListener { textView, id, _ ->
      if (id == EditorInfo.IME_ACTION_SEARCH) {
        val query = textView.text.toString()
        if (query.trim().isBlank()) {
          searchViewLayout.shake()
          return@setOnEditorActionListener true
        }
        viewModel.searchForShow(query)
        exSearchViewInput.hideKeyboard()
        exSearchViewInput.clearFocus()
      }
      true
    }

    exSearchViewIcon.onClick {
      exSearchViewInput.hideKeyboard()
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
      val color = requireContext().colorFromAttr(R.attr.colorNotification)
      setProgressBackgroundColorSchemeColor(requireContext().colorFromAttr(R.attr.colorSearchViewBackground))
      setColorSchemeColors(color, color, color)
      setProgressViewOffset(false, swipeRefreshStartOffset, swipeRefreshEndOffset)
    }
  }

  private fun setupStatusBar() {
    searchRoot.doOnApplyWindowInsets { view, insets, _, _ ->
      view.updatePadding(top = insets.systemWindowInsetTop)
    }
  }

  private fun openShowDetails(item: SearchListItem) {
    disableUi()
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
      enableUi()
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, item.show.ids.trakt.id) }
      navigateTo(R.id.actionSearchFragmentToShowDetailsFragment, bundle)
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
          exSearchViewInput.setText(item.text)
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
