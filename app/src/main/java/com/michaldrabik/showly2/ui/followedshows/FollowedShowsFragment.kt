package com.michaldrabik.showly2.ui.followedshows

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
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

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(FollowedShowsViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    viewModel.run {
      //      uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
//      clearCache()
//      loadMyShows()
    }
  }

  private fun setupView() {
    followedShowsSearchView.hint = getString(R.string.textSearchForMyShows)
    followedShowsSearchView.onClick { enterSearchMode() }
    searchViewInput.run {
      imeOptions = EditorInfo.IME_ACTION_DONE
//      addTextChangedListener { viewModel.searchMyShows(it?.toString() ?: "") }
      setOnEditorActionListener { _, _, _ ->
        clearFocus()
        hideKeyboard()
        true
      }
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

  private fun openShowDetails(show: Show) {
    followedShowsSearchView.fadeOut()
    followedShowsRoot.fadeOut {
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, show.id) }
      findNavController().navigate(R.id.actionFollowedShowsFragmentToShowDetailsFragment, bundle)
    }
    getMainActivity().hideNavigation()
  }

  override fun onTabReselected() {
//    myShowsSearchView.translationY = 0F
//    myShowsRootScroll.smoothScrollTo(0, 0)
  }
}
