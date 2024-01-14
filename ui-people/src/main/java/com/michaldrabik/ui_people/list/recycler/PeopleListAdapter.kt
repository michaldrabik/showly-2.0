package com.michaldrabik.ui_people.list.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_people.list.recycler.views.PeopleListHeaderView
import com.michaldrabik.ui_people.list.recycler.views.PeopleListItemView

class PeopleListAdapter(
  var onItemClickListener: (Person) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  companion object {
    private const val VIEW_TYPE_ITEM = 1
    private const val VIEW_TYPE_HEADER = 2
  }

  private val asyncDiffer = AsyncListDiffer(this, PeopleItemDiffCallback())

  fun setItems(items: List<PeopleListItem>) = asyncDiffer.submitList(items)

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position]) {
      is PeopleListItem.HeaderItem -> VIEW_TYPE_HEADER
      is PeopleListItem.PersonItem -> VIEW_TYPE_ITEM
      else -> throw IllegalStateException()
    }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_HEADER -> BaseViewHolder(PeopleListHeaderView(parent.context))
      VIEW_TYPE_ITEM -> BaseViewHolder(
        PeopleListItemView(parent.context).apply {
          onItemClickListener = this@PeopleListAdapter.onItemClickListener
        }
      )
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
    when (val item = asyncDiffer.currentList[position]) {
      is PeopleListItem.HeaderItem -> (holder.itemView as PeopleListHeaderView).bind(item)
      is PeopleListItem.PersonItem -> (holder.itemView as PeopleListItemView).bind(item)
    }

  override fun getItemCount() = asyncDiffer.currentList.size

  class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
