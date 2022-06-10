package com.michaldrabik.ui_show.episodes.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_model.Episode

class EpisodesAdapter(
  private val itemClickListener: (Episode, Boolean) -> Unit,
  private val itemCheckedListener: (Episode, Boolean) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val items: MutableList<EpisodeListItem> = mutableListOf()
  private var isLocked = true

  fun setItems(newItems: List<EpisodeListItem>) {
    val elements = newItems.map { it.copy(isLocked = isLocked) }
    val diffCallback = EpisodeListItemDiffCallback(items, elements)
    val diffResult = DiffUtil.calculateDiff(diffCallback)
    this.items.apply {
      clear()
      addAll(elements)
    }
    diffResult.dispatchUpdatesTo(this)
  }

  fun clearItems() {
    isLocked = true
    items.clear()
    notifyDataSetChanged()
  }

  fun toggleEpisodesLock() {
    isLocked = !isLocked
    setItems(items.toList())
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(EpisodeView(parent.context))

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder.itemView as EpisodeView).bind(items[position], itemClickListener, itemCheckedListener, isLocked)
  }

  override fun getItemCount() = items.size

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}
