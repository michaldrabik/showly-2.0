package com.michaldrabik.ui_my_shows.myshows.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_base.common.ListItem
import com.michaldrabik.ui_base.common.ListViewMode
import com.michaldrabik.ui_base.common.ListViewMode.GRID
import com.michaldrabik.ui_base.common.ListViewMode.GRID_TITLE
import com.michaldrabik.ui_base.common.ListViewMode.LIST_COMPACT
import com.michaldrabik.ui_base.common.ListViewMode.LIST_NORMAL
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem.Type
import com.michaldrabik.ui_my_shows.myshows.views.MyShowAllCompactView
import com.michaldrabik.ui_my_shows.myshows.views.MyShowAllView
import com.michaldrabik.ui_my_shows.myshows.views.MyShowGridTitleView
import com.michaldrabik.ui_my_shows.myshows.views.MyShowGridView
import com.michaldrabik.ui_my_shows.myshows.views.MyShowHeaderView
import com.michaldrabik.ui_my_shows.myshows.views.MyShowsRecentsView

class MyShowsAdapter(
  private val itemClickListener: (ListItem) -> Unit,
  private val itemLongClickListener: (ListItem) -> Unit,
  private val onSortOrderClickListener: (MyShowsSection, SortOrder, SortType) -> Unit,
  private val onListViewModeClickListener: () -> Unit,
  private val onTypeClickListener: () -> Unit,
  private val missingImageListener: (ListItem, Boolean) -> Unit,
  private val missingTranslationListener: (ListItem) -> Unit,
  listChangeListener: () -> Unit,
) : BaseAdapter<MyShowsItem>(
  listChangeListener = listChangeListener
) {

  companion object {
    private const val VIEW_TYPE_HEADER = 1
    private const val VIEW_TYPE_SHOW_ITEM = 2
    private const val VIEW_TYPE_RECENTS_SECTION = 3
  }

  override val asyncDiffer = AsyncListDiffer(this, MyShowsItemDiffCallback())

  var listViewMode: ListViewMode = LIST_NORMAL
    set(value) {
      field = value
      notifyItemRangeChanged(0, asyncDiffer.currentList.size)
    }

  fun setItems(newItems: List<MyShowsItem>, notifyChangeList: List<Type>?) {
    val notifyChange = notifyChangeList?.contains(Type.ALL_SHOWS_ITEM) == true
    super.setItems(newItems, notifyChange)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_HEADER -> BaseViewHolder(MyShowHeaderView(parent.context))
      VIEW_TYPE_RECENTS_SECTION -> BaseViewHolder(MyShowsRecentsView(parent.context))
      VIEW_TYPE_SHOW_ITEM -> BaseViewHolder(
        when (listViewMode) {
          LIST_NORMAL -> MyShowAllView(parent.context)
          LIST_COMPACT -> MyShowAllCompactView(parent.context)
          GRID -> MyShowGridView(parent.context)
          GRID_TITLE -> MyShowGridTitleView(parent.context)
        }.apply {
          itemClickListener = this@MyShowsAdapter.itemClickListener
          itemLongClickListener = this@MyShowsAdapter.itemLongClickListener
          missingImageListener = this@MyShowsAdapter.missingImageListener
          missingTranslationListener = this@MyShowsAdapter.missingTranslationListener
        }
      )
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    when (holder.itemViewType) {
      VIEW_TYPE_HEADER -> (holder.itemView as MyShowHeaderView).bind(
        item.header!!,
        listViewMode,
        onTypeClickListener,
        onSortOrderClickListener,
        onListViewModeClickListener
      )
      VIEW_TYPE_RECENTS_SECTION -> (holder.itemView as MyShowsRecentsView).bind(
        item.recentsSection!!,
        listViewMode,
        itemClickListener,
        itemLongClickListener
      )
      VIEW_TYPE_SHOW_ITEM -> when (listViewMode) {
        LIST_NORMAL -> (holder.itemView as MyShowAllView).bind(item)
        LIST_COMPACT -> (holder.itemView as MyShowAllCompactView).bind(item)
        GRID -> (holder.itemView as MyShowGridView).bind(item)
        GRID_TITLE -> (holder.itemView as MyShowGridTitleView).bind(item)
      }
    }
  }

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position].type) {
      Type.ALL_SHOWS_HEADER -> VIEW_TYPE_HEADER
      Type.ALL_SHOWS_ITEM -> VIEW_TYPE_SHOW_ITEM
      Type.RECENT_SHOWS -> VIEW_TYPE_RECENTS_SECTION
    }
}
