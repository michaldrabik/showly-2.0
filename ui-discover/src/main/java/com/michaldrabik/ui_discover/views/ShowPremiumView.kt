package com.michaldrabik.ui_discover.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_discover.R
import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import kotlinx.android.synthetic.main.view_show_premium.view.*

class ShowPremiumView : ShowView<DiscoverListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_show_premium, this)
    viewShowPremiumRoot.onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = viewShowPremiumImageStub
  override val placeholderView: ImageView = viewShowPremiumImageStub

  private lateinit var item: DiscoverListItem

  override fun bind(item: DiscoverListItem) {
    super.bind(item)
    this.item = item
  }
}
