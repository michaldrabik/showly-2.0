package com.michaldrabik.ui_movie.actors

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_model.Actor

class ActorsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val items: MutableList<Actor> = mutableListOf()

  var itemClickListener: (Actor) -> Unit = {}

  fun setItems(items: List<Actor>) {
    this.items.apply {
      clear()
      addAll(items)
    }
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(ActorView(parent.context))

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder.itemView as ActorView).bind(items[position], itemClickListener)
  }

  override fun getItemCount() = items.size

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)
}
