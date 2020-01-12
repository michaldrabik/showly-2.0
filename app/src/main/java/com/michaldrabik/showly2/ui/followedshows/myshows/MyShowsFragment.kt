package com.michaldrabik.showly2.ui.followedshows.myshows

import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.model.MyShowsSection.COMING_SOON
import com.michaldrabik.showly2.model.MyShowsSection.ENDED
import com.michaldrabik.showly2.model.MyShowsSection.RUNNING
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.ui.common.OnScrollResetListener
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.OnTraktImportListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.followedshows.FollowedShowsFragment
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsListItem
import com.michaldrabik.showly2.ui.followedshows.myshows.views.MyShowFanartView
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.fadeIf
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.visible
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.fragment_my_shows.*

class MyShowsFragment : BaseFragment<MyShowsViewModel>(), OnTabReselectedListener, OnScrollResetListener, OnTraktImportListener {

  override val layoutResId = R.layout.fragment_my_shows

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun createViewModel(provider: ViewModelProvider) =
    provider.get(MyShowsViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupSectionsViews()
    viewModel.run {
      uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      loadShows()
    }
  }

  private fun setupSectionsViews() {
    val onSectionItemClick: (MyShowsListItem) -> Unit = { openShowDetails(it.show) }
    val onSectionMissingImageListener: (MyShowsListItem, Boolean) -> Unit = { item, force ->
      viewModel.loadMissingImage(item, force)
    }
    val onSectionSortOrderChange: (MyShowsSection, SortOrder) -> Unit = { section, order ->
      viewModel.loadSortedSection(section, order)
    }
    val onSectionCollapsedListener: (MyShowsSection, Boolean) -> Unit = { section, isCollapsed ->
      viewModel.loadCollapsedSection(section, isCollapsed)
    }

    listOf(
      myShowsRunningSection,
      myShowsEndedSection,
      myShowsIncomingSection
    ).forEach {
      it.itemClickListener = onSectionItemClick
      it.missingImageListener = onSectionMissingImageListener
      it.sortSelectedListener = onSectionSortOrderChange
      it.collapseListener = onSectionCollapsedListener
    }

    myShowsAllSection.itemClickListener = onSectionItemClick
    myShowsAllSection.missingImageListener = onSectionMissingImageListener
    myShowsAllSection.sortSelectedListener = onSectionSortOrderChange
  }

  private fun render(uiModel: MyShowsUiModel) {
    uiModel.run {
      recentShows?.let {
        myShowsSearchContainer.gone()
        myShowsRecentsLabel.visible()
        myShowsRecentsContainer.visible()
        myShowsRootContent.fadeIf(it.isNotEmpty())
        myShowsEmptyView.fadeIf(it.isEmpty())
        renderRecentsContainer(it)
        (parentFragment as FollowedShowsFragment).enableSearch(it.isNotEmpty())
      }
      allShows?.let {
        myShowsAllSection.bind(it, R.string.textAllShows)
        myShowsAllSection.visibleIf(it.items.isNotEmpty())
      }
      runningShows?.let {
        myShowsRunningSection.bind(it, R.string.textRunning)
        myShowsRunningSection.visibleIf(it.items.isNotEmpty())
      }
      endedShows?.let {
        myShowsEndedSection.bind(it, R.string.textEnded)
        myShowsEndedSection.visibleIf(it.items.isNotEmpty())
      }
      incomingShows?.let {
        myShowsIncomingSection.bind(it, R.string.textIncoming)
        myShowsIncomingSection.visibleIf(it.items.isNotEmpty())
      }
      sectionsPositions?.let {
        myShowsRunningSection.scrollToPosition(it[RUNNING]?.first ?: 0, it[RUNNING]?.second ?: 0)
        myShowsEndedSection.scrollToPosition(it[ENDED]?.first ?: 0, it[ENDED]?.second ?: 0)
        myShowsIncomingSection.scrollToPosition(it[COMING_SOON]?.first ?: 0, it[COMING_SOON]?.second ?: 0)
      }
    }
  }

  private fun renderRecentsContainer(items: List<MyShowsListItem>) {
    myShowsRecentsContainer.removeAllViews()

    val context = requireContext()
    val itemHeight = context.dimenToPx(R.dimen.myShowsFanartHeight)
    val itemMargin = context.dimenToPx(R.dimen.spaceTiny)

    val clickListener: (Show) -> Unit = { openShowDetails(it) }

    items.forEachIndexed { index, item ->
      val view = MyShowFanartView(context).apply {
        layoutParams = FrameLayout.LayoutParams(0, MATCH_PARENT)
        bind(item.show, item.image, clickListener)
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
    saveToUiCache()
    (parentFragment as? FollowedShowsFragment)?.openShowDetails(show)
  }

  private fun saveToUiCache() {
    val sectionPositions = mapOf(
      RUNNING to myShowsRunningSection.getListPosition(),
      ENDED to myShowsEndedSection.getListPosition(),
      COMING_SOON to myShowsIncomingSection.getListPosition()
    )
    viewModel.saveListPosition(sectionPositions)
  }

  override fun onTabReselected() = onScrollReset()

  override fun onScrollReset() = myShowsRootScroll.smoothScrollTo(0, 0)

  override fun onTraktImportProgress() = viewModel.loadShows()
}
