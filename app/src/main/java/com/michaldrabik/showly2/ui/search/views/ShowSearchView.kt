package com.michaldrabik.showly2.ui.search.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.ui.common.views.ShowView
import com.michaldrabik.showly2.ui.search.recycler.SearchListItem
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visible
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.view_show_search.view.*

@SuppressLint("SetTextI18n")
class ShowSearchView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ShowView<SearchListItem>(context, attrs, defStyleAttr) {

  init {
    inflate(context, R.layout.view_show_search, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  override val imageView: ImageView = showSearchImage
  override val placeholderView: ImageView = showSearchPlaceholder

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

  override fun loadImage(item: SearchListItem, missingImageListener: (SearchListItem, Boolean) -> Unit) {
    if (item.image.status == Image.Status.UNAVAILABLE) {
      showSearchPlaceholder.visible()
      return
    }
    super.loadImage(item, missingImageListener)
  }

  private fun clear() {
    showSearchTitle.text = ""
    showSearchDescription.text = ""
    showSearchNetwork.text = ""
    showSearchPlaceholder.gone()
    Glide.with(this).clear(showSearchImage)
  }
}