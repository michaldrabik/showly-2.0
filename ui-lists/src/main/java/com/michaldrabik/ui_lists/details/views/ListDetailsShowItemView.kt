package com.michaldrabik.ui_lists.details.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.expandTouch
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import kotlinx.android.synthetic.main.view_list_details_show_item.view.*
import java.util.Locale.ENGLISH
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class ListDetailsShowItemView : ListDetailsItemView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_list_details_show_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    setBackgroundColor(context.colorFromAttr(android.R.attr.windowBackground))

    imageLoadCompleteListener = {
      if (item.translation == null) {
        missingTranslationListener?.invoke(item)
      }
    }

    listDetailsShowHandle.expandTouch(100)
    listDetailsShowHandle.setOnTouchListener { _, event ->
      if (item.isManageMode && event.action == ACTION_DOWN) {
        itemDragStartListener?.invoke()
      }
      false
    }

    var x = 0F
    listDetailsShowRoot.setOnTouchListener { _, event ->
      if (item.isManageMode) {
        return@setOnTouchListener false
      }
      if (event.action == ACTION_DOWN) x = event.x
      if (event.action == ACTION_UP) x = 0F
      if (event.action == ACTION_MOVE && abs(x - event.x) > 50F) {
        itemSwipeStartListener?.invoke()
        return@setOnTouchListener true
      }
      false
    }

    listDetailsShowRoot.onClick {
      if (!item.isManageMode) itemClickListener?.invoke(item)
    }
  }

  override val imageView: ImageView = listDetailsShowImage
  override val placeholderView: ImageView = listDetailsShowPlaceholder

  override fun bind(item: ListDetailsItem) {
    super.bind(item)
    Glide.with(this).clear(listDetailsShowImage)

    val show = item.requireShow()

    listDetailsShowProgress.visibleIf(item.isLoading)

    listDetailsShowTitle.text =
      if (item.translation?.title.isNullOrBlank()) show.title
      else item.translation?.title

    listDetailsShowDescription.text =
      when {
        item.translation?.overview.isNullOrBlank() -> {
          if (show.overview.isNotBlank()) show.overview
          else context.getString(R.string.textNoDescription)
        }
        else -> item.translation?.overview
      }

    listDetailsShowHeader.text =
      if (show.year > 0) context.getString(R.string.textNetwork, show.year.toString(), show.network)
      else String.format("%s", show.network)

    listDetailsShowRating.text = String.format(ENGLISH, "%.1f", show.rating)
    listDetailsShowUserRating.text = String.format(ENGLISH, "%d", item.userRating)

    listDetailsShowRank.visibleIf(item.isRankDisplayed)
    listDetailsShowRank.text = String.format(ENGLISH, "%d", item.rankDisplay)

    listDetailsShowHandle.visibleIf(item.isManageMode)
    listDetailsShowStarIcon.visibleIf(!item.isManageMode)
    listDetailsShowRating.visibleIf(!item.isManageMode)
    listDetailsShowUserStarIcon.visibleIf(!item.isManageMode && item.userRating != null)
    listDetailsShowUserRating.visibleIf(!item.isManageMode && item.userRating != null)

    with(listDetailsShowHeaderBadge) {
      val inCollection = item.isWatched || item.isWatchlist
      visibleIf(inCollection)
      if (inCollection) {
        val color = if (item.isWatched) R.color.colorAccent else R.color.colorGrayLight
        imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, color))
      }
    }

    loadImage(item)
  }
}
