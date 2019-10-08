package com.michaldrabik.showly2.ui.myshows

import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.GridLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.myshows.recycler.MyShowsListItem
import com.michaldrabik.showly2.ui.myshows.views.MyShowView
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment.Companion.ARG_SHOW_ID
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.fadeIf
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.fragment_my_shows.*

class MyShowsFragment : BaseFragment<MyShowsViewModel>(), OnTabReselectedListener {

  override val layoutResId = R.layout.fragment_my_shows

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(MyShowsViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupSectionsViews()
    viewModel.uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
    viewModel.loadMyShows()
  }

  private fun setupSectionsViews() {
    val onSectionItemClick: (MyShowsListItem) -> Unit = { openShowDetails(it.show) }
    val onSectionMissingImageListener: (MyShowsListItem, Boolean) -> Unit = { item, force ->
      viewModel.loadMissingImage(item, force)
    }
    val onSectionSortOrderChange: (MyShowsSection, SortOrder) -> Unit = { section, order ->
      viewModel.loadSortedSection(section, order)
    }

    myShowsRunningSection.itemClickListener = onSectionItemClick
    myShowsRunningSection.missingImageListener = onSectionMissingImageListener
    myShowsRunningSection.sortSelectedListener = onSectionSortOrderChange

    myShowsEndedSection.itemClickListener = onSectionItemClick
    myShowsEndedSection.missingImageListener = onSectionMissingImageListener
    myShowsEndedSection.sortSelectedListener = onSectionSortOrderChange

    myShowsIncomingSection.itemClickListener = onSectionItemClick
    myShowsIncomingSection.missingImageListener = onSectionMissingImageListener
    myShowsIncomingSection.sortSelectedListener = onSectionSortOrderChange
  }

  private fun render(uiModel: MyShowsUiModel) {
    uiModel.recentShows?.let {
      renderRecentlyAdded(it)
      myShowsRootContent.fadeIf(it.isNotEmpty())
    }
    uiModel.runningShows?.let {
      myShowsRunningSection.bind(it.items, it.section, it.sortOrder, R.string.textRunning)
      myShowsRunningSection.visibleIf(it.items.isNotEmpty())
    }
    uiModel.endedShows?.let {
      myShowsEndedSection.bind(it.items, it.section, it.sortOrder, R.string.textEnded)
      myShowsEndedSection.visibleIf(it.items.isNotEmpty())
    }
    uiModel.incomingShows?.let {
      myShowsIncomingSection.bind(it.items, it.section, it.sortOrder, R.string.textIncoming)
      myShowsIncomingSection.visibleIf(it.items.isNotEmpty())
    }
    uiModel.updateListItem?.let { item ->
      myShowsRunningSection.updateItem(item)
      myShowsEndedSection.updateItem(item)
      myShowsIncomingSection.updateItem(item)
    }
    uiModel.listPosition?.let { myShowsRootScroll.scrollTo(0, it.first) }
  }

  private fun renderRecentlyAdded(items: List<MyShowsListItem>) {
    myShowsRecentsContainer.removeAllViews()

    val context = requireContext()
    val itemHeight = context.dimenToPx(R.dimen.myShowsFanartHeight)
    val itemMargin = context.dimenToPx(R.dimen.spaceTiny)

    items.forEachIndexed { index, item ->
      val view = MyShowView(context).apply {
        layoutParams = FrameLayout.LayoutParams(0, MATCH_PARENT)
        bind(item.show, item.image)
        onItemClickListener = { openShowDetails(it) }
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

  private fun openShowDetails(show: Show) {
    //TODO Add fades transition
    myShowsRootContent.fadeOut {
      val position = myShowsRootScroll.scrollY
      viewModel.saveListPosition(position, 0)
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, show.id) }
      findNavController().navigate(R.id.actionMyShowsFragmentToShowDetailsFragment, bundle)
      getMainActivity().hideNavigation()
    }
  }

  override fun onTabReselected() = myShowsRootScroll.smoothScrollTo(0, 0)
}
