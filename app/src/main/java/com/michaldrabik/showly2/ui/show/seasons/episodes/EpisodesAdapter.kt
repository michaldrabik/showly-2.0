package com.michaldrabik.showly2.ui.show.seasons.episodes

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.model.Episode

class EpisodesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val items: MutableList<EpisodeListItem> = mutableListOf()

  var itemClickListener: (Episode) -> Unit = {}
  var itemCheckedListener: (Episode, Boolean) -> Unit = { _, _ -> }

  fun setItems(episodes: List<EpisodeListItem>) {
    this.items.apply {
      clear()
      addAll(episodes)
    }
    notifyDataSetChanged()
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