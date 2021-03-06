package com.michaldrabik.ui_lists

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.common.views.exSearchViewInput
import com.michaldrabik.ui_base.utilities.NavigationHost
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.hideKeyboard
import com.michaldrabik.ui_base.utilities.extensions.updateTopMargin
import com.michaldrabik.ui_lists.di.UiMyListsComponentProvider
import kotlinx.android.synthetic.main.fragment_my_lists.*

class MyListsFragment :
  BaseFragment<MyListsViewModel>(R.layout.fragment_my_lists),
  OnTraktSyncListener {

  override val viewModel by viewModels<MyListsViewModel> { viewModelFactory }

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiMyListsComponentProvider).provideMyListsComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupStatusBar()
    setupBackPress()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
      loadItems()
    }
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
  }

  private fun setupView() {
    fragmentMyListsSearchView.run {
      hint = getString(R.string.textSearchFor)
//      onClick { enterSearch() }
      onSettingsClickListener = { openSettings() }
      if (isTraktSyncing()) setTraktProgress(true)
    }
    fragmentMyListsModeTabs.run {
      onModeSelected = { (requireActivity() as NavigationHost).setMode(it, force = true) }
      showMovies(moviesEnabled)
      showLists(true, anchorEnd = moviesEnabled)
      selectLists()
    }
    exSearchViewInput.run {
      imeOptions = EditorInfo.IME_ACTION_DONE
      setOnEditorActionListener { _, _, _ ->
        clearFocus()
        hideKeyboard()
        true
      }
    }

    fragmentMyListsModeTabs.translationY = viewModel.tabsTranslation
    fragmentMyListsSearchView.translationY = viewModel.searchViewTranslation
  }

  private fun setupStatusBar() {
    fragmentMyListsRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      fragmentMyListsSearchView.applyWindowInsetBehaviour(dimenToPx(R.dimen.spaceNormal) + statusBarSize)
      fragmentMyListsSearchView.updateTopMargin(dimenToPx(R.dimen.spaceSmall) + statusBarSize)
      fragmentMyListsModeTabs.updateTopMargin(dimenToPx(R.dimen.collectionTabsMargin) + statusBarSize)
      fragmentMyListsEmptyView.updateTopMargin(statusBarSize)
    }
  }

  private fun setupBackPress() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      if (fragmentMyListsSearchView.isSearching) {
//        exitSearch()
      } else {
        isEnabled = false
        dispatcher.onBackPressed()
      }
    }
  }

//  private fun enterSearch() {
//    if (followedMoviesSearchView.isSearching) return
//    followedMoviesSearchView.isSearching = true
//    exSearchViewText.gone()
//    exSearchViewInput.run {
//      setText("")
//      doAfterTextChanged { viewModel.searchMovies(it?.toString() ?: "") }
//      visible()
//      showKeyboard()
//      requestFocus()
//    }
//    (exSearchViewIcon.drawable as Animatable).start()
//    exSearchViewIcon.onClick { exitSearch() }
//    hideNavigation(false)
//  }
//
//  private fun exitSearch(showNavigation: Boolean = true) {
//    followedMoviesSearchView.isSearching = false
//    exSearchViewText.visible()
//    exSearchViewInput.run {
//      setText("")
//      gone()
//      hideKeyboard()
//      clearFocus()
//    }
//    exSearchViewIcon.setImageResource(R.drawable.ic_anim_search_to_close)
//    if (showNavigation) showNavigation()
//  }

  private fun render(uiModel: MyListsUiModel) {
    uiModel.run {
      items?.let {
        fragmentMyListsEmptyView.fadeIf(it.isEmpty())
      }
    }
  }

//  private fun renderSearchResults(result: MyMoviesSearchResult) {
//    when (result.type) {
//      RESULTS -> {
//        followedMoviesSearchWrapper.visible()
//        followedMoviesPager.gone()
//        followedMoviesTabs.gone()
//        followedMoviesModeTabs.gone()
//        followedMoviesSearchEmptyView.gone()
//        renderSearchContainer(result.items)
//      }
//      NO_RESULTS -> {
//        followedMoviesSearchWrapper.gone()
//        followedMoviesPager.gone()
//        followedMoviesTabs.gone()
//        followedMoviesModeTabs.gone()
//        followedMoviesSearchEmptyView.visible()
//      }
//      EMPTY -> {
//        followedMoviesSearchWrapper.gone()
//        followedMoviesPager.visible()
//        followedMoviesTabs.visible()
//        followedMoviesModeTabs.visible()
//        followedMoviesSearchEmptyView.gone()
//      }
//    }
//
//    if (result.type != EMPTY) {
//      followedMoviesSearchView.translationY = 0F
//      followedMoviesTabs.translationY = 0F
//      followedMoviesModeTabs.translationY = 0F
//      followedMoviesSortIcon.translationY = 0F
//      childFragmentManager.fragments.forEach {
//        (it as? OnScrollResetListener)?.onScrollReset()
//      }
//    }
//  }
//
//  private fun renderSearchContainer(items: List<MyMoviesItem>) {
//    followedMoviesSearchContainer.removeAllViews()
//
//    val context = requireContext()
//    val itemHeight = context.dimenToPx(R.dimen.myMoviesFanartHeight)
//    val itemMargin = context.dimenToPx(R.dimen.spaceTiny)
//
//    val clickListener: (MyMoviesItem) -> Unit = {
//      followedMoviesRoot.hideKeyboard()
//      openMovieDetails(it.movie)
//    }
//
//    items.forEachIndexed { index, item ->
//      val view = MyMovieFanartView(context).apply {
//        layoutParams = FrameLayout.LayoutParams(0, MATCH_PARENT)
//        bind(item, clickListener)
//      }
//      val layoutParams = GridLayout.LayoutParams().apply {
//        width = 0
//        height = itemHeight
//        columnSpec = GridLayout.spec(index % 2, 1F)
//        setMargins(itemMargin, itemMargin, itemMargin, itemMargin)
//      }
//      followedMoviesSearchContainer.addView(view, layoutParams)
//    }
//  }

  private fun openSettings() {
    hideNavigation()
    navigateTo(R.id.actionListsFragmentToSettingsFragment)

    viewModel.tabsTranslation = fragmentMyListsModeTabs.translationY
    viewModel.searchViewTranslation = fragmentMyListsSearchView.translationY
  }

  fun enableSearch(enable: Boolean) {
    fragmentMyListsSearchView.isClickable = enable
    fragmentMyListsSearchView.isEnabled = enable
  }

  override fun onTraktSyncProgress() =
    fragmentMyListsSearchView.setTraktProgress(true)

  override fun onTraktSyncComplete() {
    fragmentMyListsSearchView.setTraktProgress(false)
  }
}
