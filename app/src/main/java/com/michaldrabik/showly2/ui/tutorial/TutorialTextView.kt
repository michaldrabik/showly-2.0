package com.michaldrabik.showly2.ui.tutorial

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.onClick
import kotlinx.android.synthetic.main.view_tutorial_view.view.*

class TutorialTextView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_tutorial_view, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    setBackgroundResource(R.drawable.bg_tutorial_view)
    setPadding(context.dimenToPx(R.dimen.spaceNormal))
    elevation = 10F
    setupView()
  }

  var onOkClick: (() -> Unit)? = null

  private fun setupView() {
    tutorialViewButton.onClick { onOkClick?.invoke() }
  }
}
