package com.michaldrabik.ui_lists.details.views.grid

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.views.ShowView.Companion.ASPECT_RATIO
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.screenWidth
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_lists.databinding.ViewListDetailsShowItemGridBinding
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import com.michaldrabik.ui_lists.details.views.ListDetailsItemView
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class ListDetailsGridItemView : ListDetailsItemView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewListDetailsShowItemGridBinding.inflate(LayoutInflater.from(context), this)

  private val gridPadding by lazy { context.dimenToPx(R.dimen.gridListsPadding) }
  private val width by lazy { (screenWidth().toFloat() - (2.0 * gridPadding)) / Config.LISTS_GRID_SPAN }
  private val height by lazy { width * ASPECT_RATIO }

  init {
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    binding.listDetailsGridItemRoot.onClick { itemClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
    initSwipeListener()
    initDragListener()
  }

  override val imageView: ImageView = binding.listDetailsGridItemImage
  override val placeholderView: ImageView = binding.listDetailsGridItemPlaceholder

  private fun initSwipeListener() {
    var x = 0F
    binding.listDetailsGridItemRoot.setOnTouchListener { _, event ->
      if (item.isManageMode) {
        return@setOnTouchListener false
      }
      if (event.action == MotionEvent.ACTION_DOWN) x = event.x
      if (event.action == MotionEvent.ACTION_UP) x = 0F
      if (event.action == MotionEvent.ACTION_MOVE && abs(x - event.x) > 50F) {
        itemSwipeStartListener?.invoke()
        return@setOnTouchListener true
      }
      false
    }
  }

  private fun initDragListener() {
    binding.listDetailsGridItemHandle.setOnTouchListener { _, event ->
      if (item.isManageMode && event.action == MotionEvent.ACTION_DOWN) {
        itemDragStartListener?.invoke()
      }
      false
    }
  }

  override fun bind(item: ListDetailsItem) {
    layoutParams = LayoutParams(
      (width * item.image.type.spanSize.toFloat()).toInt(),
      height.toInt()
    )

    clear()
    this.item = item

    with(binding) {
      listDetailsGridItemProgress.visibleIf(item.isLoading)

      with(listDetailsGridItemImage) {
        if (item.isShow()) setImageResource(R.drawable.ic_television)
        if (item.isMovie()) setImageResource(R.drawable.ic_film)
      }

//      if (item.sortOrder == RATING) {
//        collectionShowRating.visible()
//        collectionShowRating.text = String.format(ENGLISH, "%.1f", item.show.rating)
//      } else if (item.sortOrder == USER_RATING && item.userRating != null) {
//        collectionShowRating.visible()
//        collectionShowRating.text = String.format(ENGLISH, "%d", item.userRating)
//      } else {
//        collectionShowRating.gone()
//      }
      listDetailsGridItemHandle.visibleIf(item.isManageMode)
    }
    loadImage(item)
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    with(binding) {
      listDetailsGridItemPlaceholder.gone()
      Glide.with(this@ListDetailsGridItemView).clear(listDetailsGridItemImage)
    }
  }
}
