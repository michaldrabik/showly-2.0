package com.michaldrabik.ui_progress.progress.views

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem
import kotlinx.android.synthetic.main.view_progress_header.view.*
import java.util.Locale

@SuppressLint("SetTextI18n")
class ProgressHeaderView : LinearLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var headerClickListener: ((ProgressListItem.Header) -> Unit)? = null

  private lateinit var item: ProgressListItem.Header
  private val isRtl by lazy { TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL }

  init {
    inflate(context, R.layout.view_progress_header, this)
    layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    orientation = HORIZONTAL
    onClick { headerClickListener?.invoke(item) }
  }

  fun bind(item: ProgressListItem.Header) {
    this.item = item
    progressHeaderText.setText(item.textResId)
    val rotation = if (isRtl) -90F else 90F
    progressHeaderArrow.rotation = if (item.isCollapsed) 0F else rotation
  }
}
