package com.michaldrabik.ui_streamings.recycler

import androidx.recyclerview.widget.DiffUtil
import com.michaldrabik.ui_model.StreamingService

class StreamingItemDiffCallback : DiffUtil.ItemCallback<StreamingService>() {

  override fun areItemsTheSame(oldItem: StreamingService, newItem: StreamingService) =
    oldItem.name == newItem.name

  override fun areContentsTheSame(oldItem: StreamingService, newItem: StreamingService) =
    oldItem.name == newItem.name &&
      oldItem.imagePath == newItem.imagePath &&
      oldItem.link == newItem.link &&
      oldItem.options.size == newItem.options.size &&
      oldItem.options.containsAll(newItem.options)
}
