package com.michaldrabik.ui_people.list.recycler.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_people.R
import com.michaldrabik.ui_people.databinding.ViewPeopleListHeaderBinding
import com.michaldrabik.ui_people.list.recycler.PeopleListItem

class PeopleListHeaderView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewPeopleListHeaderBinding.inflate(LayoutInflater.from(context), this)

  private lateinit var item: PeopleListItem.HeaderItem

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  fun bind(item: PeopleListItem.HeaderItem) {
    this.item = item
    with(binding) {
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
}
