package com.michaldrabik.showly2.ui.show.quickSetup.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Season
import kotlinx.android.synthetic.main.view_quick_setup_header.view.*

class QuickSetupHeaderView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_quick_setup_header, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  fun bind(season: Season) {
    viewQuickSetupHeaderTitle.text = context.getString(R.string.textSeason, season.number)
  }
}
