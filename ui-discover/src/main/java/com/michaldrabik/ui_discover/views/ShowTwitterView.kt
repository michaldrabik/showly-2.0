package com.michaldrabik.ui_discover.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_discover.R
import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import kotlinx.android.synthetic.main.view_show_twitter.view.*

class ShowTwitterView : ShowView<DiscoverListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var twitterCancelClickListener: (() -> Unit)? = null

  init {
    inflate(context, R.layout.view_show_twitter, this)
    viewTwitterRoot.onClick { itemClickListener?.invoke(item) }
    viewTwitterCancel.onClick { twitterCancelClickListener?.invoke() }
  }

  override val imageView: ImageView = viewTwitterLogo
  override val placeholderView: ImageView = viewTwitterLogo

  private lateinit var item: DiscoverListItem

  override fun bind(item: DiscoverListItem) {
    super.bind(item)
    this.item = item
  }
}
