package com.michaldrabik.showly2.ui.show.seasons.episodes

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.model.Episode

class EpisodesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val items: MutableList<Episode> = mutableListOf()

  var itemClickListener: (Episode) -> Unit = {}

  fun setItems(items: List<Episode>) {
    this.items.apply {
      clear()
      addAll(items)
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
    (holder.itemView as EpisodeView).bind(items[position], itemClickListener)
  }

  override fun getItemCount() = items.size

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}