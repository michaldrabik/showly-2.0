package com.michaldrabik.ui_discover.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseAdapter
import com.michaldrabik.ui_discover.views.ShowFanartView
import com.michaldrabik.ui_discover.views.ShowPosterView
import com.michaldrabik.ui_discover.views.ShowPremiumView
import com.michaldrabik.ui_discover.views.ShowTwitterView
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.FANART_WIDE
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.ImageType.PREMIUM
import com.michaldrabik.ui_model.ImageType.TWITTER

class DiscoverAdapter(
  private val itemClickListener: (DiscoverListItem) -> Unit,
  private val itemLongClickListener: (DiscoverListItem) -> Unit,
  private val missingImageListener: (DiscoverListItem, Boolean) -> Unit,
  private val twitterCancelClickListener: (() -> Unit)?,
  listChangeListener: () -> Unit,
) : BaseAdapter<DiscoverListItem>(
  listChangeListener = listChangeListener
) {

  override val asyncDiffer = AsyncListDiffer(this, DiscoverItemDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
    POSTER.id -> BaseViewHolder(
      ShowPosterView(parent.context).apply {
        itemClickListener = this@DiscoverAdapter.itemClickListener
        itemLongClickListener = this@DiscoverAdapter.itemLongClickListener
        missingImageListener = this@DiscoverAdapter.missingImageListener
      }
    )
    FANART.id, FANART_WIDE.id -> BaseViewHolder(
      ShowFanartView(parent.context).apply {
        itemClickListener = this@DiscoverAdapter.itemClickListener
        itemLongClickListener = this@DiscoverAdapter.itemLongClickListener
        missingImageListener = this@DiscoverAdapter.missingImageListener
      }
    )
    TWITTER.id -> BaseViewHolder(
      ShowTwitterView(parent.context).apply {
        itemClickListener = this@DiscoverAdapter.itemClickListener
        twitterCancelClickListener = this@DiscoverAdapter.twitterCancelClickListener
      }
    )
    PREMIUM.id -> BaseViewHolder(
      ShowPremiumView(parent.context).apply {
        itemClickListener = this@DiscoverAdapter.itemClickListener
      }
    )
    else -> throw IllegalStateException("Unknown view type.")
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    when (holder.itemViewType) {
      POSTER.id ->
        (holder.itemView as ShowPosterView).bind(item)
      FANART.id, FANART_WIDE.id ->
        (holder.itemView as ShowFanartView).bind(item)
      TWITTER.id ->
        (holder.itemView as ShowTwitterView).bind(item)
      PREMIUM.id ->
        (holder.itemView as ShowPremiumView).bind(item)
    }
  }

  override fun getItemViewType(position: Int) = asyncDiffer.currentList[position].image.type.id
}
