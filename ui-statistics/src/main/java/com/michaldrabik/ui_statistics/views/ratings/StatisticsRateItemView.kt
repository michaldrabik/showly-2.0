package com.michaldrabik.ui_statistics.views.ratings

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_statistics.R
import com.michaldrabik.ui_statistics.databinding.ViewStatisticsRateItemBinding
import com.michaldrabik.ui_statistics.views.ratings.recycler.StatisticsRatingItem

class StatisticsRateItemView : ShowView<StatisticsRatingItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewStatisticsRateItemBinding.inflate(LayoutInflater.from(context), this)

  init {
    val width = context.dimenToPx(R.dimen.statisticsRatingItemWidth)
    layoutParams = LayoutParams(width, WRAP_CONTENT)
    clipChildren = false
    binding.viewRateItemImageLayout.onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = binding.viewRateItemImage
  override val placeholderView: ImageView = binding.viewRateItemPlaceholder

  private lateinit var item: StatisticsRatingItem

  override fun bind(item: StatisticsRatingItem) {
    this.item = item
    clear()
    with(binding) {
      viewRateItemTitle.text = item.show.title
      viewRateItemRating.text = "${item.rating.rating}"
    }
    loadImage(item)
  }

  private fun clear() {
    with(binding) {
      viewRateItemTitle.gone()
      viewRateItemPlaceholder.gone()
      Glide.with(this@StatisticsRateItemView).clear(viewRateItemImage)
    }
  }
}
