package com.michaldrabik.showly2.ui.followedshows.myshows.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.model.SortOrder.NAME
import com.michaldrabik.showly2.model.SortOrder.NEWEST
import com.michaldrabik.showly2.model.SortOrder.RATING
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.MyShowsBundle
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsListItem
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsSectionAdapter
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.expandTouchArea
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visible
import kotlinx.android.synthetic.main.view_my_shows_section.view.*

class MyShowsSection : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var itemClickListener: (MyShowsListItem) -> Unit = {}
  var missingImageListener: (MyShowsListItem, Boolean) -> Unit = { _, _ -> }
  var sortSelectedListener: (MyShowsSection, SortOrder) -> Unit = { _, _ -> }
  var collapseListener: (MyShowsSection, Boolean) -> Unit = { _, _ -> }

  private val padding by lazy { context.dimenToPx(R.dimen.spaceMedium) }
  private val sectionAdapter by lazy { MyShowsSectionAdapter() }
  private val sectionLayoutManager by lazy { LinearLayoutManager(context, HORIZONTAL, false) }
  private var isCollapsed: Boolean = false
  private lateinit var section: MyShowsSection

  init {
    inflate(context, R.layout.view_my_shows_section, this)
    setupView()
    setupRecycler()
  }

  fun bind(item: MyShowsBundle, labelResId: Int) {
    val (items, section, sortOrder, isCollapsed) = item
    this.section = section
    myShowsSectionLabel.text = context.getString(labelResId, items.size)
    sortOrder?.let { myShowsSectionSortView.bind(it) }
    isCollapsed?.let { bindCollapsed(it) }
    sectionAdapter.setItems(items)
  }

  private fun bindCollapsed(isCollapsed: Boolean) {
    this.isCollapsed = isCollapsed
    if (isCollapsed) {
      myShowsSectionRecycler.gone()
      myShowsSectionSortButton.gone()
      myShowsSectionCollapsedIcon.visible()
    } else {
      myShowsSectionRecycler.visible()
      myShowsSectionSortButton.visible()
      myShowsSectionCollapsedIcon.gone()
    }
  }

  fun getListPosition(): Pair<Int, Int> {
    val position = sectionLayoutManager.findFirstVisibleItemPosition()
    val offset = (sectionLayoutManager.findViewByPosition(position)?.left ?: 0) - padding
    return Pair(position, offset)
  }

  fun scrollToPosition(position: Int, offset: Int) {
    sectionLayoutManager.scrollToPositionWithOffset(position, offset)
  }

  fun isEmpty() = sectionAdapter.itemCount == 0

  private fun setupView() {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    myShowsSectionSortView.setAvailable(listOf(NAME, NEWEST, RATING))
    myShowsSectionSortButton.expandTouchArea()
    myShowsSectionSortButton.onClick { button ->
      button.gone()
      myShowsSectionSortView.run {
        fadeIn()
        sortSelectedListener = {
          myShowsSectionSortView.fadeOut()
          button.visible()
          this@MyShowsSection.sortSelectedListener(section, it)
        }
      }
    }
    myShowsSectionLabel.onClick(safe = false) { collapseListener(section, !isCollapsed) }
    myShowsSectionCollapsedIcon.onClick(safe = false) { collapseListener(section, !isCollapsed) }
  }

  private fun setupRecycler() {
    myShowsSectionRecycler.apply {
      setHasFixedSize(true)
      adapter = sectionAdapter
      layoutManager = sectionLayoutManager
      addItemDecoration(DividerItemDecoration(context, HORIZONTAL).apply {
        setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_my_shows_horizontal)!!)
      })
    }
    sectionAdapter.run {
      itemClickListener = { this@MyShowsSection.itemClickListener(it) }
      missingImageListener = { item, force -> this@MyShowsSection.missingImageListener(item, force) }
    }
  }
}
