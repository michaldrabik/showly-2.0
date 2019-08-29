package com.michaldrabik.showly2.ui.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.screenWidth

abstract class ShowView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  companion object {
    const val ASPECT_RATIO = 1.4705
  }

  protected val cornerRadius by lazy { resources.getDimensionPixelSize(R.dimen.cornerShowTile) }
  private val gridPadding by lazy { resources.getDimensionPixelSize(R.dimen.gridPadding) }
  private val gridSpan by lazy { resources.getInteger(R.integer.discoverGridSpan).toFloat() }

  protected val width by lazy { (screenWidth().toFloat() - (2.0 * gridPadding)) / gridSpan }
  protected val height by lazy { width * ASPECT_RATIO }
}