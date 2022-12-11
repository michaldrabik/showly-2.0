package com.michaldrabik.ui_my_shows.common.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.screenWidth
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.SortOrder.USER_RATING
import com.michaldrabik.ui_my_shows.common.recycler.CollectionListItem
import com.michaldrabik.ui_my_shows.databinding.ViewCollectionShowGridBinding
import kotlinx.android.synthetic.main.view_collection_show.view.*
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class CollectionShowGridView : ShowView<CollectionListItem.ShowItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewCollectionShowGridBinding.inflate(LayoutInflater.from(context), this)

  private val gridPadding by lazy { context.dimenToPx(R.dimen.gridListsPadding) }
  private val width by lazy { (screenWidth().toFloat() - (2.0 * gridPadding)) / Config.LISTS_GRID_SPAN }
  private val height by lazy { width * ASPECT_RATIO }

  init {
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    binding.collectionShowRoot.onClick { itemClickListener?.invoke(item) }
    binding.collectionShowRoot.onLongClick { itemLongClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = binding.collectionShowImage
  override val placeholderView: ImageView = binding.collectionShowPlaceholder

  private lateinit var item: CollectionListItem.ShowItem

  override fun bind(item: CollectionListItem.ShowItem) {
    layoutParams = LayoutParams(
      (width * item.image.type.spanSize.toFloat()).toInt(),
      height.toInt()
    )

    clear()
    this.item = item

    with(binding) {
      collectionShowProgress.visibleIf(item.isLoading)

      if (item.sortOrder == RATING) {
        collectionShowRating.visible()
        collectionShowRating.text = String.format(ENGLISH, "%.1f", item.show.rating)
      } else if (item.sortOrder == USER_RATING && item.userRating != null) {
        collectionShowRating.visible()
        collectionShowRating.text = String.format(ENGLISH, "%d", item.userRating)
      } else {
        collectionShowRating.gone()
      }
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
      collectionShowPlaceholder.gone()
      Glide.with(this@CollectionShowGridView).clear(collectionShowImage)
    }
  }
}
