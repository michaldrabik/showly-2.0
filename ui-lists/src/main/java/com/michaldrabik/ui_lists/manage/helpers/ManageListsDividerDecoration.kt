package com.michaldrabik.ui_lists.manage.helpers

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_lists.R

class ManageListsDividerDecoration(
  context: Context,
) : RecyclerView.ItemDecoration() {

  private var dividerDrawable: Drawable =
    ContextCompat.getDrawable(context, R.drawable.divider_manage_lists)!!

  override fun onDraw(
    c: Canvas,
    parent: RecyclerView,
    state: RecyclerView.State,
  ) {
    super.onDraw(c, parent, state)
    for (i in 0 until parent.childCount) {
      if (i != parent.childCount - 1) {
        val child: View = parent.getChildAt(i)
        val params = child.layoutParams as RecyclerView.LayoutParams

        val dividerTop: Int = child.bottom + params.bottomMargin
        val dividerBottom: Int = dividerTop + dividerDrawable.intrinsicHeight

        dividerDrawable.setBounds(0, dividerTop, parent.width, dividerBottom)
        dividerDrawable.draw(c)
      }
    }
  }
}
