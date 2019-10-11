package com.michaldrabik.showly2.ui.myshows

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.GridLayout
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.model.MyShowsSection.*
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.myshows.helpers.MyShowsSearchResult
import com.michaldrabik.showly2.ui.myshows.helpers.ResultType.*
import com.michaldrabik.showly2.ui.myshows.recycler.MyShowsListItem
import com.michaldrabik.showly2.ui.myshows.views.MyShowView
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment.Companion.ARG_SHOW_ID
import com.michaldrabik.showly2.utilities.extensions.*
import kotlinx.android.synthetic.main.fragment_my_shows.*
import kotlinx.android.synthetic.main.view_search.*

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
    setupView()
    setupSectionsViews()
    viewModel.uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
    viewModel.loadMyShows()
  }

  private fun setupView() {
    myShowsSearchView.onClick { enterSearchMode() }
    searchViewInput.run {
      imeOptions = EditorInfo.IME_ACTION_NONE
      addTextChangedListener { viewModel.searchMyShows(it?.toString() ?: "") }
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
    (searchViewIcon.drawable as AnimatedVectorDrawable).start()
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
    uiModel.searchResult?.let { renderSearchResults(it) }
    uiModel.recentShows?.let {
      myShowsSearchContainer.gone()
      myShowsRecentsLabel.visible()
      myShowsRecentsContainer.visible()
      renderFanartContainer(it, myShowsRecentsContainer)
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
    uiModel.sectionsPositions?.let {
      myShowsRunningSection.scrollToPosition(it[RUNNING]?.first ?: 0, it[RUNNING]?.second ?: 0)
      myShowsEndedSection.scrollToPosition(it[ENDED]?.first ?: 0, it[ENDED]?.second ?: 0)
      myShowsIncomingSection.scrollToPosition(it[COMING_SOON]?.first ?: 0, it[COMING_SOON]?.second ?: 0)
    }
    uiModel.mainListPosition?.let { myShowsRootScroll.scrollTo(0, it.first) }
  }

  private fun renderSearchResults(result: MyShowsSearchResult) {
    when (result.type) {
      RESULTS -> {
        myShowsRecentsLabel.gone()
        myShowsSearchContainer.visible()
        myShowsRecentsContainer.gone()
        myShowsRunningSection.gone()
        myShowsEndedSection.gone()
        myShowsIncomingSection.gone()
        myShowsSearchEmptyView.gone()
        renderFanartContainer(result.items, myShowsSearchContainer)
      }
      NO_RESULTS -> {
        myShowsRecentsLabel.gone()
        myShowsRecentsContainer.gone()
        myShowsSearchContainer.gone()
        myShowsRunningSection.gone()
        myShowsEndedSection.gone()
        myShowsIncomingSection.gone()
        myShowsSearchEmptyView.visible()
      }
      EMPTY -> {
        myShowsRecentsLabel.visible()
        myShowsSearchContainer.gone()
        myShowsRecentsContainer.visible()
        myShowsRunningSection.visible()
        myShowsEndedSection.visible()
        myShowsIncomingSection.visible()
        myShowsSearchEmptyView.gone()
      }
    }
    myShowsRootScroll.scrollTo(0, 0)
  }

  private fun renderFanartContainer(items: List<MyShowsListItem>, container: GridLayout) {
    container.removeAllViews()

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
      container.addView(view, layoutParams)
    }
  }

  private fun openShowDetails(show: Show) {
    myShowsSearchView.fadeOut()
    myShowsRootContent.fadeOut {
      saveToUiCache()
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, show.id) }
      findNavController().navigate(R.id.actionMyShowsFragmentToShowDetailsFragment, bundle)
      getMainActivity().hideNavigation()
    }
  }

  private fun saveToUiCache() {
    val mainPosition = myShowsRootScroll.scrollY
    val sectionPositions = mapOf(
      RUNNING to myShowsRunningSection.getListPosition(),
      ENDED to myShowsEndedSection.getListPosition(),
      COMING_SOON to myShowsIncomingSection.getListPosition()
    )
    viewModel.saveListPosition(mainPosition, sectionPositions)
  }

  override fun onTabReselected() = myShowsRootScroll.smoothScrollTo(0, 0)
}
