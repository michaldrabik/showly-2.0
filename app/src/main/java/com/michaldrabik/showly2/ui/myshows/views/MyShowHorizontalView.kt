package com.michaldrabik.showly2.ui.myshows.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.common.views.ShowView
import com.michaldrabik.showly2.ui.myshows.MyShowListItem
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import kotlinx.android.synthetic.main.view_my_show.view.*

class MyShowHorizontalView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ShowView<MyShowListItem>(context, attrs, defStyleAttr) {

  init {
    inflate(context, R.layout.view_my_show, this)
    val width = context.dimenToPx(R.dimen.myShowsPosterWidth)
    layoutParams = LayoutParams(width, MATCH_PARENT)
  }

  override val imageView: ImageView = myShowImage
  override val placeholderView: ImageView = myShowPlaceholder

  override fun bind(
    item: MyShowListItem,
    missingImageListener: (MyShowListItem, Boolean) -> Unit,
    itemClickListener: (MyShowListItem) -> Unit
  ) {

  }

  fun bind(listItem: MyShowListItem) {
    clear()
    loadImage(listItem, { item, force -> })
    //TODO missing image handling
  }

  private fun clear() {
    Glide.with(this).clear(myShowImage)
  }
}