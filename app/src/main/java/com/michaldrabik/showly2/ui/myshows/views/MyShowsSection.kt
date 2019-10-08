package com.michaldrabik.showly2.ui.myshows.views

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
import com.michaldrabik.showly2.ui.myshows.recycler.MyShowsListItem
import com.michaldrabik.showly2.ui.myshows.recycler.MyShowsSectionAdapter
import com.michaldrabik.showly2.utilities.extensions.*
import kotlinx.android.synthetic.main.view_my_shows_section.view.*

class MyShowsSection @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

  private val sectionAdapter = MyShowsSectionAdapter()

  var itemClickListener: (MyShowsListItem) -> Unit = {}
  var missingImageListener: (MyShowsListItem, Boolean) -> Unit = { _, _ -> }
  var sortSelectedListener: (MyShowsSection, SortOrder) -> Unit = { _, _ -> }

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

  fun updateItem(item: MyShowsListItem) = sectionAdapter.updateItem(item)

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
      layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
      addItemDecoration(DividerItemDecoration(context, HORIZONTAL).apply {
        setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_my_shows_horizontal)!!)
      })
    }
    sectionAdapter.run {
      itemClickListener = { itemClickListener(it) }
      missingImageListener = { item, force -> missingImageListener(item, force) }
    }
  }
}