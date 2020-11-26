package com.michaldrabik.ui_discover_movies

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import com.michaldrabik.showly2.R
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.disableUi
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.enableUi
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_discover_movies.di.UiDiscoverMoviesComponentProvider
import kotlinx.android.synthetic.main.fragment_discover_movies.*

class DiscoverMoviesFragment : BaseFragment<DiscoverMoviesViewModel>(R.layout.fragment_discover_movies), OnTabReselectedListener {

  override val viewModel by viewModels<DiscoverMoviesViewModel> { viewModelFactory }

  private val swipeRefreshStartOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshStartOffset) }
  private val swipeRefreshEndOffset by lazy { requireContext().dimenToPx(R.dimen.swipeRefreshEndOffset) }

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiDiscoverMoviesComponentProvider).provideDiscoverMoviesComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onPause() {
    enableUi()
    super.onPause()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupStatusBar()
  }

  private fun setupView() {
    discoverMoviesSearchView.run {
      sortIconVisible = true
      settingsIconVisible = false
      isClickable = false
      onClick { navigateToSearch() }
//      onSortClickListener = { toggleFiltersView() }
//      translationY = searchViewPosition
    }
  }

  private fun setupStatusBar() {
    discoverMoviesRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      discoverMoviesRecycler
        .updatePadding(top = statusBarSize + dimenToPx(R.dimen.discoverMoviesRecyclerPadding))
      (discoverMoviesSearchView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.spaceSmall))
//      (discoverFiltersView.layoutParams as ViewGroup.MarginLayoutParams)
//        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.searchViewHeight))
      (discoverMoviesTabsView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.showsMoviesTabsMargin))
      discoverMoviesSwipeRefresh.setProgressViewOffset(
        true,
        swipeRefreshStartOffset + statusBarSize,
        swipeRefreshEndOffset
      )
    }
  }

  private fun navigateToSearch() {
    disableUi()
//    saveUi()
    hideNavigation()
//    discoverFiltersView.fadeOut()
    discoverMoviesRecycler.fadeOut(duration = 200) {
      super.navigateTo(R.id.actionDiscoverMoviesFragmentToSearchFragment, null)
    }
  }

  override fun onTabReselected() = navigateToSearch()
}
