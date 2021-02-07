package com.michaldrabik.ui_gallery.fanart.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.utilities.extensions.replace
import com.michaldrabik.ui_gallery.fanart.recycler.views.ArtGalleryFanartView
import com.michaldrabik.ui_gallery.fanart.recycler.views.ArtGalleryPosterView
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.FANART_WIDE
import com.michaldrabik.ui_model.ImageType.POSTER

class ArtGalleryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  companion object {
    private const val VIEW_TYPE_POSTER = 0
    private const val VIEW_TYPE_FANART = 1
  }

  var onItemClickListener: (() -> Unit)? = null

  private lateinit var type: ImageType
  private val items = mutableListOf<Image>()

  fun setItems(items: List<Image>, type: ImageType) {
    this.type = type
    this.items.replace(items)
    notifyDataSetChanged()
  }

  fun getItem(position: Int) = items[position]

  override fun getItemViewType(position: Int) = when (type) {
    POSTER -> VIEW_TYPE_POSTER
    FANART, FANART_WIDE -> VIEW_TYPE_FANART
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_POSTER -> ViewHolderShow(
        ArtGalleryPosterView(parent.context).apply {
          onItemClickListener = this@ArtGalleryAdapter.onItemClickListener
        }
      )
      VIEW_TYPE_FANART -> ViewHolderShow(
        ArtGalleryFanartView(parent.context).apply {
          onItemClickListener = this@ArtGalleryAdapter.onItemClickListener
        }
      )
      else -> error("Invalid type")
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (val itemView = holder.itemView) {
      is ArtGalleryPosterView -> itemView.bind(items[position])
      is ArtGalleryFanartView -> itemView.bind(items[position])
    }
  }

  class ViewHolderShow(itemView: View) : RecyclerView.ViewHolder(itemView)

  override fun getItemCount() = items.size
}
