package com.michaldrabik.showly2.ui.followedshows.myshows.views.section

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.common.views.ShowView
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsItem
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visible
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.Image.Status
import kotlinx.android.synthetic.main.view_my_shows_section_item.view.*

class MyShowsSectionItemView : ShowView<MyShowsItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_my_shows_section_item, this)
    val width = context.dimenToPx(R.dimen.myShowsPosterWidth)
    layoutParams = LayoutParams(width, MATCH_PARENT)
    onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = myShowImage
  override val placeholderView: ImageView = myShowPlaceholder

  private lateinit var item: MyShowsItem

  override fun bind(
    item: MyShowsItem,
    missingImageListener: (MyShowsItem, Boolean) -> Unit
  ) {
    clear()
    this.item = item
    myShowTitle.text = item.show.title
    myShowProgress.visibleIf(item.isLoading)
    loadImage(item, missingImageListener)
  }

  override fun loadImage(item: MyShowsItem, missingImageListener: (MyShowsItem, Boolean) -> Unit) {
    if (item.image.status == Status.UNAVAILABLE) {
      myShowTitle.visible()
      myShowRoot.setBackgroundResource(R.drawable.bg_show_view_placeholder)
    }
    super.loadImage(item, missingImageListener)
  }

  private fun clear() {
    myShowTitle.gone()
    placeholderView.gone()
    Glide.with(this).clear(myShowImage)
  }
}
