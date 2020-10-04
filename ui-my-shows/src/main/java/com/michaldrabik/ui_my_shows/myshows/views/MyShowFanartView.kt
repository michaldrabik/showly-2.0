package com.michaldrabik.ui_my_shows.myshows.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.michaldrabik.common.Config
import com.michaldrabik.common.Config.IMAGE_FADE_DURATION_MS
import com.michaldrabik.ui_base.utilities.extensions.*
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import kotlinx.android.synthetic.main.view_my_shows_section_item.view.*

class MyShowFanartView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_my_shows_section_item, this)
  }

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.myShowsFanartCorner) }

  fun bind(showItem: MyShowsItem, clickListener: (MyShowsItem) -> Unit) {
    clear()
    myShowTitle.text = showItem.show.title
    myShowTitle.visible()
    onClick { clickListener(showItem) }
    loadImage(showItem.image)
  }

  private fun loadImage(image: Image) {
    if (image.status != Image.Status.AVAILABLE) {
      myShowPlaceholder.visible()
      myShowRoot.setBackgroundResource(R.drawable.bg_show_view_placeholder)
      return
    }

    val url = "${Config.TVDB_IMAGE_BASE_BANNERS_URL}${image.fileUrl}"

    Glide.with(this)
      .load(url)
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .transition(DrawableTransitionOptions.withCrossFade(IMAGE_FADE_DURATION_MS))
      .withFailListener {
        myShowPlaceholder.visible()
        myShowImage.gone()
      }
      .into(myShowImage)
  }

  private fun clear() {
    myShowPlaceholder.gone()
    myShowTitle.text = ""
    myShowRoot.setBackgroundResource(0)
    Glide.with(this).clear(myShowImage)
  }
}
