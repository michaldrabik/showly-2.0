package com.michaldrabik.showly2.ui.shows.seasons

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class SeasonsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val items: MutableList<SeasonListItem> = mutableListOf()

  var itemClickListener: (SeasonListItem) -> Unit = {}

  fun setItems(items: List<SeasonListItem>) {
    this.items.apply {
      clear()
      addAll(items)
    }
    notifyItemRangeInserted(0, items.size)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(SeasonView(parent.context))

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder.itemView as SeasonView).bind(items[position], itemClickListener)
  }

  override fun getItemCount() = items.size

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}