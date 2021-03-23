package com.michaldrabik.ui_lists.manage.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.manage.recycler.ManageListsItem
import kotlinx.android.synthetic.main.view_manage_lists_item.view.*

@SuppressLint("SetTextI18n")
class ManageListsItemView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var itemCheckListener: ((ManageListsItem, Boolean) -> Unit)? = null
  var isCheckEnabled = false

  init {
    inflate(context, R.layout.view_manage_lists_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    manageListsItemCheckbox.setOnCheckedChangeListener { _, isChecked ->
      if (isCheckEnabled) itemCheckListener?.invoke(item, isChecked)
    }
  }

  private lateinit var item: ManageListsItem

  fun bind(item: ManageListsItem) {
    this.item = item
    isCheckEnabled = false
    manageListsItemCheckbox.text = " ${item.list.name}"
    manageListsItemCheckbox.isChecked = item.isChecked
    manageListsItemCheckbox.isEnabled = item.isEnabled
    isCheckEnabled = true
  }
}
