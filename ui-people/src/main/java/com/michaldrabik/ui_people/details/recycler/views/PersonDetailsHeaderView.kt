package com.michaldrabik.ui_people.details.recycler.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.ui_people.R
import com.michaldrabik.ui_people.details.recycler.PersonDetailsItem
import kotlinx.android.synthetic.main.view_person_details_header.view.*

class PersonDetailsHeaderView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_person_details_header, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  fun bind(item: PersonDetailsItem.CreditsHeader) {
    viewPersonDetailsHeader.text =
      if (item.year != null) item.year.toString()
      else context.getString(R.string.textMovieStatusInProduction)
  }
}
