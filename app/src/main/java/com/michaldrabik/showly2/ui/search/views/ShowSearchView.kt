package com.michaldrabik.showly2.ui.search.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.ui.common.views.ShowView
import com.michaldrabik.showly2.ui.search.recycler.SearchListItem
import com.michaldrabik.showly2.utilities.extensions.*
import kotlinx.android.synthetic.main.view_show_search.view.*

@SuppressLint("SetTextI18n")
class ShowSearchView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ShowView<SearchListItem>(context, attrs, defStyleAttr) {

  init {
    inflate(context, R.layout.view_show_search, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  override fun bind(
    item: SearchListItem,
    missingImageListener: (SearchListItem, Boolean) -> Unit,
    itemClickListener: (SearchListItem) -> Unit
  ) {
    clear()
    showSearchTitle.text = item.show.title
    showSearchDescription.text = item.show.overview
    showSearchNetwork.text = "${item.show.network} (${item.show.year})"

    showSearchDescription.visibleIf(item.show.overview.isNotBlank())
    showSearchNetwork.visibleIf(item.show.network.isNotBlank())
    if (!item.isLoading) loadImage(item, missingImageListener)

    showSearchRoot.onClick { itemClickListener(item) }
  }

  private fun loadImage(item: SearchListItem, missingImageListener: (SearchListItem, Boolean) -> Unit) {
    if (item.image.status == Image.Status.UNAVAILABLE) {
      showSearchPlaceholder.visible()
      return
    }

    val url = when {
      item.image.status == Image.Status.UNKNOWN -> "${Config.TVDB_IMAGE_BASE_POSTER_URL}${item.show.ids.tvdb}-1.jpg"
      else -> "${Config.TVDB_IMAGE_BASE_URL}${item.image.thumbnailUrl}"
    }

    Glide.with(this)
      .load(url)
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .transition(DrawableTransitionOptions.withCrossFade(200))
      .withFailListener { onImageLoadFail(item, missingImageListener) }
      .into(showSearchImage)
  }

  private fun onImageLoadFail(item: SearchListItem, missingImageListener: (SearchListItem, Boolean) -> Unit) {
    if (item.image.status == Image.Status.AVAILABLE) {
      showSearchPlaceholder.visible()
      return
    }
    val force = item.image.status != Image.Status.UNAVAILABLE
    missingImageListener(item, force)
  }

  private fun clear() {
    showSearchTitle.text = ""
    showSearchDescription.text = ""
    showSearchNetwork.text = ""
    showSearchPlaceholder.gone()
    Glide.with(this).clear(showSearchImage)
  }
}