package com.michaldrabik.showly2.ui.shows.actors

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.showly2.model.Actor

class ActorsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val items: MutableList<Actor> = mutableListOf()

  fun setItems(items: List<Actor>) {
    this.items.apply {
      clear()
      addAll(items)
    }
    notifyItemRangeInserted(0, items.size)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(ActorView(parent.context))

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder.itemView as ActorView).bind(items[position])
  }

  override fun getItemCount() = items.size

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}