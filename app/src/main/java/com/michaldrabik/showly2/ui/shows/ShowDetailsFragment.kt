package com.michaldrabik.showly2.ui.shows

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_show_details.*

@SuppressLint("SetTextI18n")
class ShowDetailsFragment : BaseFragment<ShowDetailsViewModel>() {

  override val layoutResId = R.layout.fragment_show_details

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    appComponent().inject(this)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(ShowDetailsViewModel::class.java)
      .apply {
        uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.loadShowDetails()
  }

  private fun render(uiModel: ShowDetailsUiModel) {
    uiModel.show?.let {
      showDetailsTitle.text = it.title
      showDetailsDescription.text = it.overview
      showDetailsStatus.text = it.status
      showDetailsExtraInfo.text = "${it.network} | ${it.runtime} min | ${it.genres.take(2).joinToString(", ")}"
    }
  }
}
