package com.michaldrabik.showly2.ui.search.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.search.recycler.SearchListItem
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.view_show_search.view.*

class ShowSearchView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

  private val padding by lazy { context.dimenToPx(R.dimen.spaceMedium) }

  init {
    inflate(context, R.layout.view_show_search, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    setPadding(padding)
  }

  fun bind(
    item: SearchListItem,
    missingImageListener: (SearchListItem, Boolean) -> Unit,
    itemClickListener: (SearchListItem) -> Unit
  ) {
    clear()
    showSearchTitle.text = item.show.title
    showSearchDescription.text = item.show.overview
    showSearchNetwork.text = item.show.network

    showSearchDescription.visibleIf(item.show.overview.isNotBlank())
    showSearchNetwork.visibleIf(item.show.network.isNotBlank())
  }

  private fun clear() {
    showSearchTitle.text = ""
    showSearchDescription.text = ""
    showSearchNetwork.text = ""
    Glide.with(this).clear(showSearchImage)
  }

}