package com.michaldrabik.ui_discover.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_discover.databinding.ViewShowTwitterBinding
import com.michaldrabik.ui_discover.recycler.DiscoverListItem

class ShowTwitterView : ShowView<DiscoverListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewShowTwitterBinding.inflate(LayoutInflater.from(context), this)

  var twitterCancelClickListener: (() -> Unit)? = null

  init {
    with(binding) {
      viewTwitterRoot.onClick { itemClickListener?.invoke(item) }
      viewTwitterCancel.onClick { twitterCancelClickListener?.invoke() }
    }
  }

  override val imageView: ImageView = binding.viewTwitterLogo
  override val placeholderView: ImageView = binding.viewTwitterLogo

  private lateinit var item: DiscoverListItem

  override fun bind(item: DiscoverListItem) {
    super.bind(item)
    this.item = item
  }
}
