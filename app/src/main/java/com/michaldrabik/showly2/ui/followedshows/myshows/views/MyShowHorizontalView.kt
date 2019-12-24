package com.michaldrabik.showly2.ui.followedshows.myshows.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Image.Status
import com.michaldrabik.showly2.ui.common.views.ShowView
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsListItem
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visible
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.view_my_show.view.*

class MyShowHorizontalView : ShowView<MyShowsListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_my_show, this)
    val width = context.dimenToPx(R.dimen.myShowsPosterWidth)
    layoutParams = LayoutParams(width, MATCH_PARENT)
  }

  override val imageView: ImageView = myShowImage
  override val placeholderView: ImageView = myShowPlaceholder

  override fun bind(
    item: MyShowsListItem,
    missingImageListener: (MyShowsListItem, Boolean) -> Unit,
    itemClickListener: (MyShowsListItem) -> Unit
  ) {
    clear()
    myShowTitle.text = item.show.title
    myShowProgress.visibleIf(item.isLoading)
    onClick { itemClickListener(item) }
    loadImage(item, missingImageListener)
  }

  override fun loadImage(item: MyShowsListItem, missingImageListener: (MyShowsListItem, Boolean) -> Unit) {
    if (item.image.status == Status.UNAVAILABLE) {
      myShowTitle.visible()
    }
    super.loadImage(item, missingImageListener)
  }

  private fun clear() {
    Glide.with(this).clear(myShowImage)
  }
}
