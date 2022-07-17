package com.michaldrabik.ui_news.views.item

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Build.VERSION_CODES
import android.text.Html
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.invisible
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_model.NewsItem.Type
import com.michaldrabik.ui_news.R
import com.michaldrabik.ui_news.databinding.ViewNewsItemCardBinding
import com.michaldrabik.ui_news.recycler.NewsListItem
import java.util.Locale

@SuppressLint("SetTextI18n")
class NewsItemCardView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.newsCardCornerRadius) }

  var itemClickListener: ((NewsListItem) -> Unit)? = null

  private val binding = ViewNewsItemCardBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    clipChildren = false
    clipToPadding = false

    binding.newsItemRoot.onClick { itemClickListener?.invoke(item) }
  }

  private lateinit var item: NewsListItem

  @Suppress("DEPRECATION")
  fun bind(item: NewsListItem) {
    clear()
    this.item = item

    val icon = when (item.item.type) {
      Type.SHOW -> R.drawable.ic_television
      Type.MOVIE -> R.drawable.ic_film
    }

    with(binding) {
      newsItemHeaderIcon.setImageResource(icon)
      if (icon == R.drawable.ic_television) {
        newsItemHeaderIcon.translationY = -2F
      }
      newsItemPlaceholder.setImageResource(icon)

      newsItemTitle.text = when {
        Build.VERSION.SDK_INT >= VERSION_CODES.N -> Html.fromHtml(item.item.title, Html.FROM_HTML_MODE_LEGACY)
        else -> Html.fromHtml(item.item.title)
      }

      val relativeTime = DateUtils.getRelativeTimeSpanString(item.item.datedAt.toMillis()).toString().lowercase(Locale.ROOT)
      newsItemHeader.text = item.dateFormat.format(item.item.datedAt.toLocalZone()).capitalizeWords()
      newsItemSubheader.text = "~ $relativeTime"
    }

    loadImage(item)
  }

  private fun loadImage(item: NewsListItem) {
    if (item.item.image == null) {
      binding.newsItemPlaceholder.visible()
      binding.newsItemImage.invisible()
      return
    }

    Glide.with(this@NewsItemCardView)
      .load(item.item.image)
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .transition(withCrossFade(Config.IMAGE_FADE_DURATION_MS))
      .withSuccessListener {
        binding.newsItemPlayIcon.visibleIf(item.item.isVideo)
      }
      .withFailListener {
        binding.newsItemPlaceholder.fadeIn()
        binding.newsItemImage.invisible()
      }
      .into(binding.newsItemImage)
  }

  private fun clear() {
    with(binding) {
      Glide.with(this@NewsItemCardView).clear(newsItemImage)
      newsItemPlayIcon.gone()
      newsItemPlaceholder.gone()
      newsItemImage.visible()
      newsItemHeaderIcon.translationY = 0f
    }
  }
}
