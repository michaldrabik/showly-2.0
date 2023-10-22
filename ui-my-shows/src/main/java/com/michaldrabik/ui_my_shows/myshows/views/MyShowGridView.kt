package com.michaldrabik.ui_my_shows.myshows.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.isTablet
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.screenWidth
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.SortOrder.USER_RATING
import com.michaldrabik.ui_my_shows.databinding.ViewCollectionShowGridBinding
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class MyShowGridView : ShowView<MyShowsItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewCollectionShowGridBinding.inflate(LayoutInflater.from(context), this)

  private val spaceTiny by lazy { context.dimenToPx(R.dimen.spaceTiny) }
  private val span by lazy { if (context.isTablet()) Config.LISTS_GRID_SPAN_TABLET else Config.LISTS_GRID_SPAN }

  private val width by lazy {
    val screenMargin = 2.0 * context.dimenToPx(R.dimen.screenMarginHorizontal)
    (screenWidth().toFloat() - screenMargin) / span
  }
  private val height by lazy { width * ASPECT_RATIO }

  init {
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)

    clipChildren = false
    clipToPadding = false

    with(binding) {
      collectionShowRoot.onClick { itemClickListener?.invoke(item) }
      collectionShowRoot.onLongClick { itemLongClickListener?.invoke(item) }
    }

    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = binding.collectionShowImage
  override val placeholderView: ImageView = binding.collectionShowPlaceholder

  private lateinit var item: MyShowsItem

  fun bind(item: MyShowsItem, position: Int) {
    layoutParams = LayoutParams(width.toInt(), height.toInt())
    when {
      position % span == 0 -> updatePadding(left = 0, right = spaceTiny)
      (position + 1) % span == 0 -> updatePadding(left = spaceTiny, right = 0)
      else -> updatePadding(left = spaceTiny, right = spaceTiny)
    }

    clear()
    this.item = item

    with(binding) {
      collectionShowProgress.visibleIf(item.isLoading)

      if (item.sortOrder == RATING) {
        bindRating(item)
      } else if (item.sortOrder == USER_RATING && item.userRating != null) {
        collectionShowRating.visible()
        collectionShowRating.text = String.format(ENGLISH, "%d", item.userRating)
      } else {
        collectionShowRating.gone()
      }
    }

    loadImage(item)
  }

  private fun bindRating(item: MyShowsItem) {
    with(binding) {
      var rating = String.format(ENGLISH, "%.1f", item.show.rating)

      if (item.spoilers.isSpoilerRatingsHidden) {
        collectionShowRating.tag = rating
        rating = Config.SPOILERS_RATINGS_HIDE_SYMBOL

        if (item.spoilers.isSpoilerTapToReveal) {
          with(collectionShowRating) {
            onClick {
              tag?.let { text = it.toString() }
              isClickable = false
            }
          }
        }
      }

      collectionShowRating.visible()
      collectionShowRating.text = rating
    }
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    with(binding) {
      collectionShowPlaceholder.gone()
      Glide.with(this@MyShowGridView).clear(collectionShowImage)
    }
  }
}
