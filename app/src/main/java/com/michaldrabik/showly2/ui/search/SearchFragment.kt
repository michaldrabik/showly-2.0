package com.michaldrabik.showly2.ui.search

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.hideKeyboard
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.showKeyboard
import com.michaldrabik.showly2.utilities.extensions.visible
import kotlinx.android.synthetic.main.view_search.*

class SearchFragment : BaseFragment<SearchViewModel>() {

  override val layoutResId = R.layout.fragment_search

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
  }

  private fun setupView() {
    (searchViewIcon.drawable as Animatable).start()
    searchViewInput.visible()
    searchViewText.gone()
    searchViewInput.showKeyboard()
    searchViewInput.requestFocus()

    searchViewIcon.onClick {
      searchViewInput.hideKeyboard()
      requireActivity().onBackPressed()
    }
  }

  private fun render(uiModel: SearchUiModel) {

  }
}
