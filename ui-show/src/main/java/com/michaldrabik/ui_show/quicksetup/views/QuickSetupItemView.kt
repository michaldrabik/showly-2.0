package com.michaldrabik.ui_show.quicksetup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_show.R
import com.michaldrabik.ui_show.databinding.ViewQuickSetupItemBinding
import java.util.Locale.ENGLISH

class QuickSetupItemView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewQuickSetupItemBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  fun bind(
    item: Episode,
    isChecked: Boolean,
    onItemClickListener: ((Episode, Boolean) -> Unit)?
  ) {
    val titleColor = if (isChecked) R.attr.colorAccent else android.R.attr.textColorSecondary
    val subTitleColor = if (isChecked) R.attr.colorAccent else android.R.attr.textColorPrimary

    with(binding) {
      viewQuickSetupItemRadio.isChecked = isChecked
      viewQuickSetupItemTitle.run {
        text = String.format(ENGLISH, context.getString(R.string.textEpisode), item.number)
        setTextColor(context.colorFromAttr(titleColor))
      }
      viewQuickSetupItemSubtitle.run {
        text = item.title
        setTextColor(context.colorFromAttr(subTitleColor))
      }
    }

    onClick { onItemClickListener?.invoke(item, isChecked) }
  }
}
