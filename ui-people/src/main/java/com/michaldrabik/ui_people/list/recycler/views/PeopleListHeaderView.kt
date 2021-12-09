package com.michaldrabik.ui_people.list.recycler.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_people.R
import com.michaldrabik.ui_people.list.recycler.PeopleListItem
import kotlinx.android.synthetic.main.view_people_list_header.view.*

class PeopleListHeaderView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private lateinit var item: PeopleListItem.HeaderItem

  init {
    inflate(context, R.layout.view_people_list_header, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  fun bind(item: PeopleListItem.HeaderItem) {
    this.item = item
    viewPeopleListHeaderTitle.text = when (item.department) {
      Person.Department.ACTING -> context.getString(R.string.textActing)
      Person.Department.DIRECTING -> context.getString(R.string.textDirecting)
      Person.Department.WRITING -> context.getString(R.string.textWriting)
      Person.Department.SOUND -> context.getString(R.string.textMusic)
      else -> "-"
    }
    viewPeopleListHeaderSubtitle.text = item.mediaTitle
  }
}
