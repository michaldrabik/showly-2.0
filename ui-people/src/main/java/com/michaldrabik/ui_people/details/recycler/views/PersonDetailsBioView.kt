package com.michaldrabik.ui_people.details.recycler.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.ui_people.R
import com.michaldrabik.ui_people.details.recycler.PersonDetailsItem
import kotlinx.android.synthetic.main.view_person_details_bio.view.*

class PersonDetailsBioView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_person_details_bio, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    viewPersonDetailsBio.setInitialLines(5)
  }

  fun bind(item: PersonDetailsItem.MainBio) {
    when {
      item.biography.isNullOrBlank() -> viewPersonDetailsBio.text = context.getString(R.string.textNoDescription)
      !item.biographyTranslation.isNullOrBlank() -> viewPersonDetailsBio.text = item.biographyTranslation
      else -> viewPersonDetailsBio.text = item.biography
    }
  }
}
