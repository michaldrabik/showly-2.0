package com.michaldrabik.ui_discover.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_discover.databinding.ViewShowPremiumBinding
import com.michaldrabik.ui_discover.recycler.DiscoverListItem

class ShowPremiumView : ShowView<DiscoverListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewShowPremiumBinding.inflate(LayoutInflater.from(context), this, true)

  init {
    binding.viewShowPremiumRoot.onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = binding.viewShowPremiumImageStub
  override val placeholderView: ImageView = binding.viewShowPremiumImageStub

  private lateinit var item: DiscoverListItem

  override fun bind(item: DiscoverListItem) {
    super.bind(item)
    this.item = item
  }
}
