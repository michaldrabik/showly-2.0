package com.michaldrabik.ui_lists.manage.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.ui_lists.databinding.ViewManageListsItemBinding
import com.michaldrabik.ui_lists.manage.recycler.ManageListsItem

@SuppressLint("SetTextI18n")
class ManageListsItemView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewManageListsItemBinding.inflate(LayoutInflater.from(context), this)

  var itemCheckListener: ((ManageListsItem, Boolean) -> Unit)? = null
  private var isCheckEnabled = false

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    binding.manageListsItemCheckbox.setOnCheckedChangeListener { _, isChecked ->
      if (isCheckEnabled) itemCheckListener?.invoke(item, isChecked)
    }
  }

  private lateinit var item: ManageListsItem

  fun bind(item: ManageListsItem) {
    this.item = item
    isCheckEnabled = false
    with(binding) {
      manageListsItemCheckbox.text = " ${item.list.name}"
      manageListsItemCheckbox.isChecked = item.isChecked
      manageListsItemCheckbox.isEnabled = item.isEnabled
    }
    isCheckEnabled = true
  }
}
