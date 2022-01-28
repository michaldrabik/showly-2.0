package com.michaldrabik.ui_people.gallery.recycler

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.utilities.extensions.replace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_people.gallery.recycler.views.PersonGalleryImageView

class PersonGalleryAdapter(
  val onItemClickListener: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  val items = mutableListOf<Image>()

  @SuppressLint("NotifyDataSetChanged")
  fun setItems(items: List<Image>) {
    this.items.replace(items)
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolder(
      PersonGalleryImageView(parent.context).apply {
        onItemClickListener = this@PersonGalleryAdapter.onItemClickListener
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
    (holder.itemView as PersonGalleryImageView).bind(items[position])

  override fun getItemCount() = items.size

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
