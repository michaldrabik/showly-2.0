package com.michaldrabik.showly2.ui.followedshows

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.followedshows.myshows.MyShowsFragment
import com.michaldrabik.showly2.ui.followedshows.watchlater.LaterShowsFragment
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment.Companion.ARG_SHOW_ID
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.hideKeyboard
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.showKeyboard
import com.michaldrabik.showly2.utilities.extensions.visible
import kotlinx.android.synthetic.main.fragment_followed_shows.*
import kotlinx.android.synthetic.main.view_search.*

class FollowedShowsFragment : BaseFragment<FollowedShowsViewModel>(), OnTabReselectedListener {

  override val layoutResId = R.layout.fragment_followed_shows

  private lateinit var pagesAdapter: FollowedPagesAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(FollowedShowsViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupPager()
    viewModel.run {
      uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      clearCache()
    }
  }

  private fun setupView() {
    followedShowsSearchView.hint = getString(R.string.textSearchForMyShows)
    followedShowsSearchView.onClick { enterSearchMode() }
    searchViewInput.run {
      imeOptions = EditorInfo.IME_ACTION_DONE
      addTextChangedListener { viewModel.searchMyShows(it?.toString() ?: "") }
      setOnEditorActionListener { _, _, _ ->
        clearFocus()
        hideKeyboard()
        true
      }
    }
  }

  private fun setupPager() {
    //TODO Look for possible optimizations
    pagesAdapter = FollowedPagesAdapter(this)
    pagesAdapter.addPages(
      MyShowsFragment(),
      LaterShowsFragment()
    )
    followedShowsPager.run {
      isUserInputEnabled = false
      adapter = pagesAdapter
    }
  }

  private fun enterSearchMode() {
    searchViewText.gone()
    searchViewInput.run {
      setText("")
      visible()
      showKeyboard()
      requestFocus()
    }
    getMainActivity().hideNavigation()
    (searchViewIcon.drawable as Animatable).start()
    searchViewIcon.onClick { exitSearchMode() }
  }

  private fun exitSearchMode() {
    searchViewText.visible()
    searchViewInput.run {
      setText("")
      gone()
      hideKeyboard()
      clearFocus()
    }
    getMainActivity().showNavigation()
    searchViewIcon.setImageResource(R.drawable.ic_anim_search_to_close)
  }

  private fun render(uiModel: FollowedShowsUiModel) {
    uiModel.run {
      searchResult?.let { }
    }
  }

//  private fun renderSearchResults(result: MyShowsSearchResult) {
//    when (result.type) {
//      ResultType.RESULTS -> {
//        myShowsRecentsLabel.gone()
//        myShowsSearchContainer.visible()
//        myShowsRecentsContainer.gone()
//        myShowsRunningSection.gone()
//        myShowsEndedSection.gone()
//        myShowsIncomingSection.gone()
//        myShowsSearchEmptyView.gone()
//        renderFanartContainer(result.items, myShowsSearchContainer)
//      }
//      ResultType.NO_RESULTS -> {
//        myShowsRecentsLabel.gone()
//        myShowsRecentsContainer.gone()
//        myShowsSearchContainer.gone()
//        myShowsRunningSection.gone()
//        myShowsEndedSection.gone()
//        myShowsIncomingSection.gone()
//        myShowsSearchEmptyView.visible()
//      }
//      ResultType.EMPTY -> {
//        myShowsRecentsLabel.visible()
//        myShowsSearchContainer.gone()
//        myShowsRecentsContainer.visible()
//        myShowsRunningSection.run { if (!isEmpty()) visible() }
//        myShowsEndedSection.run { if (!isEmpty()) visible() }
//        myShowsIncomingSection.run { if (!isEmpty()) visible() }
//        myShowsSearchEmptyView.gone()
//      }
//    }
//    myShowsRootScroll.scrollTo(0, 0)
//  }

  fun openShowDetails(show: Show) {
    followedShowsPager.fadeOut {
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, show.id) }
      findNavController().navigate(R.id.actionFollowedShowsFragmentToShowDetailsFragment, bundle)
    }
    getMainActivity().hideNavigation()
  }

  fun enableSearch(enable: Boolean) {
    followedShowsSearchView.isClickable = enable
    followedShowsSearchView.isEnabled = enable
  }

  override fun onTabReselected() {
    followedShowsSearchView.translationY = 0F
    childFragmentManager.fragments.forEach {
      (it as? OnTabReselectedListener)?.onTabReselected()
    }
  }
}
