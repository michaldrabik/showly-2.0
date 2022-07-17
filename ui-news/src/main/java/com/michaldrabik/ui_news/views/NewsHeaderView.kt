package com.michaldrabik.ui_news.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.michaldrabik.ui_base.common.behaviour.ScrollableViewBehaviour
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_news.R
import com.michaldrabik.ui_news.databinding.ViewNewsHeaderBinding
import com.michaldrabik.ui_news.views.item.NewsItemViewType
import com.michaldrabik.ui_news.views.item.NewsItemViewType.CARD
import com.michaldrabik.ui_news.views.item.NewsItemViewType.ROW

class NewsHeaderView : FrameLayout, CoordinatorLayout.AttachedBehavior {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onSettingsClickListener: (() -> Unit)? = null
  var onViewTypeClickListener: (() -> Unit)? = null

  private val binding = ViewNewsHeaderBinding.inflate(LayoutInflater.from(context), this)

  init {
    binding.viewNewsHeaderSettingsIcon.onClick { onSettingsClickListener?.invoke() }
    binding.viewNewsHeaderViewTypeIcon.onClick { onViewTypeClickListener?.invoke() }
  }

  fun setViewType(itemViewType: NewsItemViewType) {
    binding.viewNewsHeaderViewTypeIcon.setImageResource(
      when (itemViewType) {
        ROW -> R.drawable.ic_view_cards
        CARD -> R.drawable.iv_view_list
      }
    )
  }

  override fun getBehavior() = ScrollableViewBehaviour()
}
