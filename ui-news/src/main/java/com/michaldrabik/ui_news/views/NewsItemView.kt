package com.michaldrabik.ui_news.views

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Build.VERSION_CODES
import android.text.Html
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.invisible
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_model.NewsItem.Type
import com.michaldrabik.ui_news.R
import com.michaldrabik.ui_news.recycler.NewsListItem
import kotlinx.android.synthetic.main.view_news_item.view.*

@SuppressLint("SetTextI18n")
class NewsItemView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.listItemCorner) }

  init {
    inflate(context, R.layout.view_news_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  private lateinit var item: NewsListItem

  fun bind(item: NewsListItem) {
    clear()
    this.item = item

    val icon = when (item.item.type) {
      Type.SHOW -> R.drawable.ic_television
      Type.MOVIE -> R.drawable.ic_film
    }
    newsItemHeaderIcon.setImageResource(icon)
    newsItemPlaceholder.setImageResource(icon)

    newsItemTitle.text = when {
      Build.VERSION.SDK_INT >= VERSION_CODES.N -> Html.fromHtml(item.item.title, Html.FROM_HTML_MODE_LEGACY)
      else -> Html.fromHtml(item.item.title)
    }
    newsItemHeader.text = "~1 day ago"
    newsItemSubheader.text = item.dateFormat.format(item.item.datedAt.toLocalZone()).capitalizeWords()

    loadImage(item)
  }

  private fun loadImage(item: NewsListItem) {
    if (item.item.image == null) {
      newsItemPlaceholder.visible()
      newsItemImage.invisible()
      return
    }

    Glide.with(this)
      .load(item.item.image)
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .transition(withCrossFade(Config.IMAGE_FADE_DURATION_MS))
      .withSuccessListener {
        newsItemPlayIcon.visibleIf(item.item.isVideo)
      }
      .withFailListener {
        newsItemPlaceholder.fadeIn()
        newsItemImage.invisible()
      }
      .into(newsItemImage)
  }

  private fun clear() {
    Glide.with(this).clear(newsItemImage)
    newsItemPlayIcon.gone()
    newsItemPlaceholder.gone()
    newsItemImage.visible()
  }
}
