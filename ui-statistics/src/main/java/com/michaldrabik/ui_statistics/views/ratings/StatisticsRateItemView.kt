package com.michaldrabik.ui_statistics.views.ratings

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_statistics.R
import com.michaldrabik.ui_statistics.views.ratings.recycler.StatisticsRatingItem
import kotlinx.android.synthetic.main.view_statistics_rate_item.view.*

class StatisticsRateItemView : ShowView<StatisticsRatingItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_statistics_rate_item, this)
    val width = context.dimenToPx(R.dimen.statisticsRatingItemWidth)
    layoutParams = LayoutParams(width, WRAP_CONTENT)
    clipChildren = false
    viewRateItemImageLayout.onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = viewRateItemImage
  override val placeholderView: ImageView = viewRateItemPlaceholder

  private lateinit var item: StatisticsRatingItem

  override fun bind(item: StatisticsRatingItem) {
    this.item = item
    clear()
    viewRateItemTitle.text = item.show.title
    viewRateItemRating.text = "${item.rating.rating}"
    loadImage(item)
  }

  private fun clear() {
    viewRateItemTitle.gone()
    viewRateItemPlaceholder.gone()
    Glide.with(this).clear(viewRateItemImage)
  }
}
