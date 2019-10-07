package com.michaldrabik.showly2.ui.myshows.views

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.myshows.MyShowsListItem
import com.michaldrabik.showly2.ui.myshows.recycler.MyShowsSectionAdapter
import kotlinx.android.synthetic.main.view_my_shows_section.view.*

class MyShowsSection @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

  private val sectionAdapter = MyShowsSectionAdapter()

  var itemClickListener: (MyShowsListItem) -> Unit = {}
  var missingImageListener: (MyShowsListItem, Boolean) -> Unit = { _, _ -> }

  init {
    inflate(context, R.layout.view_my_shows_section, this)
    setupRecycler()
  }

  fun bind(items: List<MyShowsListItem>, labelResId: Int) {
    myShowsSectionLabel.text = context.getString(labelResId, items.size)
    sectionAdapter.run {
      clearItems()
      setItems(items)
    }
  }

  fun updateItem(item: MyShowsListItem) = sectionAdapter.updateItem(item)

  private fun setupRecycler() {
    myShowsSectionRecycler.apply {
      setHasFixedSize(true)
      adapter = sectionAdapter
      layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
      addItemDecoration(DividerItemDecoration(context, HORIZONTAL).apply {
        setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_my_shows_horizontal)!!)
      })
    }
    sectionAdapter.itemClickListener = { itemClickListener(it) }
    sectionAdapter.missingImageListener = { item, force -> missingImageListener(item, force) }
  }
}