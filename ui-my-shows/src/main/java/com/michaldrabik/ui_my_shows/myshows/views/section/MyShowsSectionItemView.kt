package com.michaldrabik.ui_my_shows.myshows.views.section

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.ImageStatus
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import kotlinx.android.synthetic.main.view_my_shows_section_item.view.*

class MyShowsSectionItemView : ShowView<MyShowsItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_my_shows_section_item, this)
    val width = context.dimenToPx(R.dimen.myShowsPosterWidth)
    layoutParams = LayoutParams(width, MATCH_PARENT)
    setBackgroundResource(R.drawable.bg_media_view_elevation)
    elevation = context.dimenToPx(R.dimen.elevationSmall).toFloat()
    onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = myShowImage
  override val placeholderView: ImageView = myShowPlaceholder

  private lateinit var item: MyShowsItem

  override fun bind(item: MyShowsItem) {
    clear()
    this.item = item
    myShowTitle.text = item.show.title
    myShowProgress.visibleIf(item.isLoading)
    loadImage(item)
  }

  override fun loadImage(item: MyShowsItem) {
    if (item.image.status == ImageStatus.UNAVAILABLE) {
      myShowTitle.visible()
      myShowRoot.setBackgroundResource(R.drawable.bg_media_view_placeholder)
    }
    super.loadImage(item)
  }

  private fun clear() {
    myShowTitle.gone()
    placeholderView.gone()
    Glide.with(this).clear(myShowImage)
  }
}
