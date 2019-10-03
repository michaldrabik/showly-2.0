package com.michaldrabik.showly2.ui.myshows

import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.GridLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.myshows.views.MyShowFanartView
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import kotlinx.android.synthetic.main.fragment_my_shows.*

class MyShowsFragment : BaseFragment<MyShowsViewModel>() {

  override val layoutResId = com.michaldrabik.showly2.R.layout.fragment_my_shows

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(MyShowsViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    viewModel.uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
    viewModel.loadMyShows()
  }

  private fun setupView() {

  }

  private fun render(uiModel: MyShowsUiModel) {
    uiModel.recentShows?.let { renderRecentlyAdded(it) }
  }

  private fun renderRecentlyAdded(items: List<MyShowListItem>) {
    myShowsRecentsContainer.removeAllViews()

    val context = requireContext()
    val itemHeight = context.dimenToPx(R.dimen.myShowFanartHeight)
    val itemMargin = context.dimenToPx(R.dimen.spaceTiny)

    items.forEachIndexed { index, item ->
      val view = MyShowFanartView(context).apply {
        layoutParams = FrameLayout.LayoutParams(0, MATCH_PARENT)
        bind(item.show, item.image)
      }
      val layoutParams = GridLayout.LayoutParams().apply {
        width = 0
        height = itemHeight
        columnSpec = GridLayout.spec(index % 2, 1F)
        setMargins(itemMargin, itemMargin, itemMargin, itemMargin)
      }
      myShowsRecentsContainer.addView(view, layoutParams)
    }
  }
}
