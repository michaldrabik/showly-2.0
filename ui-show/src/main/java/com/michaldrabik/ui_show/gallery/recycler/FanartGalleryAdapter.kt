package com.michaldrabik.ui_show.gallery.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.utilities.extensions.replace
import com.michaldrabik.ui_model.Image

class FanartGalleryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  var onItemClickListener: (() -> Unit)? = null

  private val items = mutableListOf<Image>()

  fun setItems(items: List<Image>) {
    this.items.replace(items)
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolderShow(FanartGalleryImageView(parent.context).apply {
      onItemClickListener = this@FanartGalleryAdapter.onItemClickListener
    })

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    (holder.itemView as FanartGalleryImageView).bind(items[position])
  }

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)

  override fun getItemCount() = items.size
}
