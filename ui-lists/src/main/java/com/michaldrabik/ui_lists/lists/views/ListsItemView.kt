package com.michaldrabik.ui_lists.lists.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.lists.recycler.ListsItem
import com.michaldrabik.ui_model.SortOrder.DATE_UPDATED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import kotlinx.android.synthetic.main.view_lists_item.view.*

@SuppressLint("SetTextI18n")
class ListsItemView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var itemClickListener: ((ListsItem) -> Unit)? = null

  init {
    inflate(context, R.layout.view_lists_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    listsItemRoot.onClick { itemClickListener?.invoke(item) }
  }

  private lateinit var item: ListsItem

  fun bind(item: ListsItem) {
    this.item = item

    listsItemTitle.text = item.list.name
    with(listsItemDescription) {
      text = item.list.description
      visibleIf(!item.list.description.isNullOrBlank())
    }

    listsItemHeader.visibleIf(item.sortOrder != NAME)
    listsItemHeader.text = when (item.sortOrder) {
      NAME -> ""
      NEWEST -> item.dateFormat?.format(item.list.createdAt)?.capitalizeWords()
      DATE_UPDATED -> item.dateFormat?.format(item.list.updatedAt)?.capitalizeWords()
      else -> throw IllegalStateException()
    }

//    val hasItems = item.list.itemCount > 0
//    listsItemPlaceholder.visibleIf(!hasItems)
//    listsItemImage.visibleIf(hasItems)
  }
}
