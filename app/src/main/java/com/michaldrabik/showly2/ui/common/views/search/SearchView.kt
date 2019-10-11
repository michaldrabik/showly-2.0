package com.michaldrabik.showly2.ui.common.views.search

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import kotlinx.android.synthetic.main.view_search.view.*

class SearchView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {

  init {
    inflate(context, R.layout.view_search, this)
  }

  var hint: String
    get() = searchViewInput.hint.toString()
    set(value) {
      searchViewInput.hint = value
      searchViewText.text = value
    }

  override fun getBehavior() = SearchViewBehaviour(context.dimenToPx(R.dimen.spaceSmall))

  override fun setEnabled(enabled: Boolean) {
    searchViewInput.isEnabled = enabled
  }
}