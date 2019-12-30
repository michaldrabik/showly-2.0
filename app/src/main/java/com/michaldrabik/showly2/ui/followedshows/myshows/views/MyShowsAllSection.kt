package com.michaldrabik.showly2.ui.followedshows.myshows.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.model.SortOrder.NAME
import com.michaldrabik.showly2.model.SortOrder.NEWEST
import com.michaldrabik.showly2.model.SortOrder.RATING
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.MyShowsBundle
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsAllSectionAdapter
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsListItem
import com.michaldrabik.showly2.utilities.extensions.expandTouchArea
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visible
import kotlinx.android.synthetic.main.view_my_shows_all_section.view.*

class MyShowsAllSection : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var itemClickListener: (MyShowsListItem) -> Unit = {}
  var missingImageListener: (MyShowsListItem, Boolean) -> Unit = { _, _ -> }
  var sortSelectedListener: (MyShowsSection, SortOrder) -> Unit = { _, _ -> }

  private val sectionAdapter by lazy { MyShowsAllSectionAdapter() }
  private val sectionLayoutManager by lazy { LinearLayoutManager(context, VERTICAL, false) }
  private lateinit var section: MyShowsSection

  init {
    inflate(context, R.layout.view_my_shows_all_section, this)
    setupView()
    setupRecycler()
  }

  fun bind(item: MyShowsBundle, labelResId: Int) {
    val (items, section, sortOrder) = item
    this.section = section
    myShowsAllSectionLabel.text = context.getString(labelResId, items.size)
    sortOrder?.let { myShowsAllSectionSortView.bind(it) }
    sectionAdapter.setItems(items)
  }

  fun isEmpty() = sectionAdapter.itemCount == 0

  private fun setupView() {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    myShowsAllSectionSortView.setAvailable(listOf(NAME, NEWEST, RATING))
    myShowsAllSectionSortButton.expandTouchArea()
    myShowsAllSectionSortButton.onClick { button ->
      button.gone()
      myShowsAllSectionSortView.run {
        fadeIn()
        sortSelectedListener = {
          myShowsAllSectionSortView.fadeOut()
          button.visible()
          this@MyShowsAllSection.sortSelectedListener(section, it)
        }
      }
    }
  }

  private fun setupRecycler() {
    myShowsAllSectionRecycler.apply {
      setHasFixedSize(true)
      adapter = sectionAdapter
      layoutManager = sectionLayoutManager
    }
    sectionAdapter.run {
      itemClickListener = { this@MyShowsAllSection.itemClickListener(it) }
      missingImageListener = { item, force -> this@MyShowsAllSection.missingImageListener(item, force) }
    }
  }
}
