package com.michaldrabik.showly2.ui.followedshows.myshows.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.common.views.ShowView
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsItem
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.view_my_show_all.view.*

@SuppressLint("SetTextI18n")
class MyShowAllView : ShowView<MyShowsItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_my_show_all, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  override val imageView: ImageView = myShowAllImage
  override val placeholderView: ImageView = myShowAllPlaceholder

  override fun bind(
    item: MyShowsItem,
    missingImageListener: (MyShowsItem, Boolean) -> Unit,
    itemClickListener: (MyShowsItem) -> Unit
  ) {
    clear()
    myShowAllProgress.visibleIf(item.isLoading)
    myShowAllTitle.text = item.show.title
    myShowAllDescription.text = item.show.overview
    val year = if (item.show.year > 0) " (${item.show.year})" else ""
    myShowAllNetwork.text = "${item.show.network}$year"
    myShowAllRating.text = String.format("%.1f", item.show.rating)

    myShowAllDescription.visibleIf(item.show.overview.isNotBlank())
    myShowAllNetwork.visibleIf(item.show.network.isNotBlank())
    myShowAllRoot.onClick { itemClickListener(item) }

    loadImage(item, missingImageListener)
  }

  private fun clear() {
    myShowAllTitle.text = ""
    myShowAllDescription.text = ""
    myShowAllNetwork.text = ""
    myShowAllRating.text = ""
    myShowAllPlaceholder.gone()
    Glide.with(this).clear(myShowAllImage)
  }
}
