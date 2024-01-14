package com.michaldrabik.ui_people.gallery.recycler

import androidx.recyclerview.widget.DiffUtil
import com.michaldrabik.ui_model.Image

class ImageItemDiffCallback : DiffUtil.ItemCallback<Image>() {

  override fun areItemsTheSame(oldItem: Image, newItem: Image) =
    oldItem.id == newItem.id

  override fun areContentsTheSame(oldItem: Image, newItem: Image) =
    oldItem == newItem
}
