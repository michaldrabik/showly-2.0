package com.michaldrabik.ui_my_shows.watchlist.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.screenWidth
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_my_shows.databinding.ViewWatchlistShowGridTitleBinding
import com.michaldrabik.ui_my_shows.watchlist.recycler.WatchlistListItem

@SuppressLint("SetTextI18n")
class WatchlistShowGridTitleView : ShowView<WatchlistListItem.ShowItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewWatchlistShowGridTitleBinding.inflate(LayoutInflater.from(context), this)

  private val gridPadding by lazy { context.dimenToPx(R.dimen.gridListsPadding) }
  private val width by lazy { (screenWidth().toFloat() - (2.0 * gridPadding)) / Config.LISTS_GRID_SPAN }
  private val height by lazy { width * 1.7305 }

  init {
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    binding.watchlistShowRoot.onClick { itemClickListener?.invoke(item) }
    binding.watchlistShowRoot.onLongClick { itemLongClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = binding.watchlistShowImage
  override val placeholderView: ImageView = binding.watchlistShowPlaceholder

  private lateinit var item: WatchlistListItem.ShowItem

  override fun bind(item: WatchlistListItem.ShowItem) {
    layoutParams = LayoutParams(
      (width * item.image.type.spanSize.toFloat()).toInt(),
      height.toInt()
    )

    clear()
    this.item = item

    with(binding) {
      watchlistShowProgress.visibleIf(item.isLoading)
      watchlistShowTitle.text =
        if (item.translation?.title.isNullOrBlank()) item.show.title
        else item.translation?.title
    }

    loadImage(item)
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    with(binding) {
      watchlistShowTitle.text = ""
      watchlistShowPlaceholder.gone()
      Glide.with(this@WatchlistShowGridTitleView).clear(watchlistShowImage)
    }
  }
}
