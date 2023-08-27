package com.michaldrabik.ui_my_shows.myshows.filters.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import com.michaldrabik.ui_base.utilities.extensions.addRipple
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.databinding.ViewMyShowsTypeFilterItemBinding

class MyShowsFilterItemView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewMyShowsTypeFilterItemBinding.inflate(LayoutInflater.from(context), this)

  var onItemClickListener: ((MyShowsSection) -> Unit)? = null

  lateinit var sectionType: MyShowsSection
  var isChecked: Boolean = false

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    val paddingHorizontal = context.dimenToPx(R.dimen.spaceNormal)
    setPadding(paddingHorizontal, 0, paddingHorizontal, 0)
    addRipple()
    onClick(safe = false) { onItemClickListener?.invoke(sectionType) }
  }

  fun bind(
    sectionType: MyShowsSection,
    isChecked: Boolean,
  ) {
    this.sectionType = sectionType
    this.isChecked = isChecked

    binding.viewMyShowsTypeItemBadge.visibleIf(isChecked)

    with(binding.viewMyShowsTypeItemTitle) {
      val color = if (isChecked) android.R.attr.textColorPrimary else android.R.attr.textColorSecondary
      val typeface = if (isChecked) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
      setTextColor(context.colorFromAttr(color))
      setTypeface(typeface)
      text = context.getString(sectionType.displayString)
    }
  }
}
