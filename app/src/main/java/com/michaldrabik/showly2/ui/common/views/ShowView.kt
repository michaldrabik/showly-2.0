package com.michaldrabik.showly2.ui.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.discover.recycler.ListItem
import com.michaldrabik.showly2.utilities.extensions.screenWidth

abstract class ShowView<Item : ListItem> @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  companion object {
    const val ASPECT_RATIO = 1.4705
  }

  protected val cornerRadius by lazy { resources.getDimensionPixelSize(R.dimen.showTileCorner) }
  private val gridPadding by lazy { resources.getDimensionPixelSize(R.dimen.gridPadding) }
  private val gridSpan by lazy { resources.getInteger(R.integer.discoverGridSpan).toFloat() }

  private val width by lazy { (screenWidth().toFloat() - (2.0 * gridPadding)) / gridSpan }
  private val height by lazy { width * ASPECT_RATIO }

  open fun bind(
    item: Item,
    missingImageListener: (Item, Boolean) -> Unit,
    itemClickListener: (Item) -> Unit
  ) {
    layoutParams = LayoutParams((width * item.image.type.spanSize.toFloat()).toInt(), height.toInt())
  }
}