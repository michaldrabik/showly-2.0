package com.michaldrabik.showly2.ui.show.seasons.episodes

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.model.Episode

class EpisodesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val items: MutableList<EpisodeListItem> = mutableListOf()

  var itemClickListener: (Episode, Boolean) -> Unit = { _, _ -> }
  var itemCheckedListener: (Episode, Boolean) -> Unit = { _, _ -> }

  fun setItems(newItems: List<EpisodeListItem>) {
    val diffCallback = EpisodeListItemDiffCallback(items, newItems)
    val diffResult = DiffUtil.calculateDiff(diffCallback)
    this.items.apply {
      clear()
      addAll(newItems)
    }
    diffResult.dispatchUpdatesTo(this)
  }

  fun clearItems() {
    items.clear()
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(EpisodeView(parent.context))

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder.itemView as EpisodeView).bind(items[position], itemClickListener, itemCheckedListener)
  }

  override fun getItemCount() = items.size

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}