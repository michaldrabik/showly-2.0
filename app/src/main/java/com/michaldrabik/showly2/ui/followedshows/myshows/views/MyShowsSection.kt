package com.michaldrabik.showly2.ui.followedshows.myshows.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.model.SortOrder
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

  private val padding by lazy { context.dimenToPx(R.dimen.spaceMedium) }
  private val sectionAdapter by lazy { MyShowsSectionAdapter() }
  private val sectionLayoutManager by lazy { LinearLayoutManager(context, HORIZONTAL, false) }
  private lateinit var section: MyShowsSection

  init {
    inflate(context, R.layout.view_my_shows_section, this)
    setupView()
    setupRecycler()
  }

  fun bind(
    items: List<MyShowsListItem>,
    section: MyShowsSection,
    sortOrder: SortOrder,
    labelResId: Int
  ) {
    this.section = section
    myShowsSectionLabel.text = context.getString(labelResId, items.size)
    myShowsSectionSortView.bind(sortOrder)
    sectionAdapter.setItems(items)
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
    layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
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