package com.michaldrabik.showly2.ui.show.seasons

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class SeasonsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val items: MutableList<SeasonListItem> = mutableListOf()

  var itemClickListener: (SeasonListItem) -> Unit = {}

  fun setItems(newItems: List<SeasonListItem>) {
    val diffCallback = SeasonListItemDiffCallback(items, newItems)
    val diffResult = DiffUtil.calculateDiff(diffCallback)
    this.items.apply {
      clear()
      addAll(newItems)
    }
    diffResult.dispatchUpdatesTo(this)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(SeasonView(parent.context))

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder.itemView as SeasonView).bind(items[position], itemClickListener)
  }

  override fun getItemCount() = items.size

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}