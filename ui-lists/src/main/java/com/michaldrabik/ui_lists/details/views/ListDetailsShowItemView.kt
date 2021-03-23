package com.michaldrabik.ui_lists.details.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.expandTouch
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import kotlinx.android.synthetic.main.view_list_details_show_item.view.*
import java.util.Locale.ENGLISH

@SuppressLint("ClickableViewAccessibility")
class ListDetailsShowItemView : ListDetailsItemView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_list_details_show_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    setBackgroundColor(context.colorFromAttr(android.R.attr.colorBackground))

    imageLoadCompleteListener = {
      if (item.translation == null) {
        missingTranslationListener?.invoke(item)
      }
    }

    listDetailsShowHandle.expandTouch(100)
    listDetailsShowHandle.setOnTouchListener { _, event ->
      if (item.isManageMode && event.action == MotionEvent.ACTION_DOWN) {
        itemDragStartListener?.invoke()
      }
      false
    }

    listDetailsShowRoot.onClick {
      if (!item.isManageMode) itemClickListener?.invoke(item)
    }
  }

  override val imageView: ImageView = listDetailsShowImage
  override val placeholderView: ImageView = listDetailsShowPlaceholder

  override fun bind(item: ListDetailsItem, position: Int) {
    super.bind(item, position)
    Glide.with(this).clear(listDetailsShowImage)

    val show = item.requireShow()

    listDetailsShowProgress.visibleIf(item.isLoading)

    listDetailsShowTitle.text =
      if (item.translation?.title.isNullOrBlank()) show.title
      else item.translation?.title?.capitalizeWords()

    listDetailsShowDescription.text =
      when {
        item.translation?.overview.isNullOrBlank() -> {
          if (show.overview.isNotBlank()) show.overview
          else context.getString(R.string.textNoDescription)
        }
        else -> item.translation?.overview
      }

    listDetailsShowHeader.text =
      if (show.year > 0) context.getString(R.string.textNetwork, show.network, show.year.toString())
      else String.format("%s", show.network)

    listDetailsShowRating.text = String.format(ENGLISH, "%.1f", show.rating)

    listDetailsShowRank.visibleIf(item.isRankDisplayed)
    listDetailsShowRank.text = String.format(ENGLISH, "%d", position + 1)

    listDetailsShowHandle.visibleIf(item.isManageMode)
    listDetailsShowStarIcon.visibleIf(!item.isManageMode)
    listDetailsShowRating.visibleIf(!item.isManageMode)

    loadImage(item)
  }
}
