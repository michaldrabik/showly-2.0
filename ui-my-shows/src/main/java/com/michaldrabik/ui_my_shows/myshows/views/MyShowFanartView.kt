package com.michaldrabik.ui_my_shows.myshows.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.michaldrabik.common.Config.IMAGE_FADE_DURATION_MS
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageStatus
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import kotlinx.android.synthetic.main.view_my_shows_fanart.view.*

class MyShowFanartView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_my_shows_fanart, this)
    setBackgroundResource(R.drawable.bg_media_view_elevation)
    elevation = context.dimenToPx(R.dimen.elevationSmall).toFloat()
  }

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.myShowsFanartCorner) }

  fun bind(
    showItem: MyShowsItem,
    clickListener: (MyShowsItem) -> Unit,
    longClickListener: (MyShowsItem, View) -> Unit
  ) {
    clear()
    myShowFanartTitle.visible()
    myShowFanartTitle.text =
      if (showItem.translation?.title.isNullOrBlank()) showItem.show.title
      else showItem.translation?.title
    onClick { clickListener(showItem) }
    onLongClick { longClickListener(showItem, it) }
    loadImage(showItem.image)
  }

  private fun loadImage(image: Image) {
    if (image.status != ImageStatus.AVAILABLE) {
      myShowFanartPlaceholder.visible()
      myShowFanartRoot.setBackgroundResource(R.drawable.bg_media_view_placeholder)
      return
    }
    Glide.with(this)
      .load(image.fullFileUrl)
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .transition(DrawableTransitionOptions.withCrossFade(IMAGE_FADE_DURATION_MS))
      .withFailListener {
        myShowFanartPlaceholder.visible()
        myShowFanartImage.gone()
      }
      .into(myShowFanartImage)
  }

  private fun clear() {
    myShowFanartPlaceholder.gone()
    myShowFanartTitle.text = ""
    myShowFanartRoot.setBackgroundResource(0)
    Glide.with(this).clear(myShowFanartImage)
  }
}
