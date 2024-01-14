package com.michaldrabik.ui_people.gallery.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_people.gallery.recycler.views.PersonGalleryImageView

class PersonGalleryAdapter(
  val onItemClickListener: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val asyncDiffer = AsyncListDiffer(this, ImageItemDiffCallback())

  fun setItems(items: List<Image>) {
    asyncDiffer.submitList(items)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolder(
      PersonGalleryImageView(parent.context).apply {
        onItemClickListener = this@PersonGalleryAdapter.onItemClickListener
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
    (holder.itemView as PersonGalleryImageView).bind(asyncDiffer.currentList[position])

  fun getItem(index: Int) = asyncDiffer.currentList.getOrNull(index)

  override fun getItemCount() = asyncDiffer.currentList.size

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
