package com.michaldrabik.ui_lists.details.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_lists.R
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import kotlinx.android.synthetic.main.view_list_details_show_item.view.*
import java.util.Locale.ENGLISH

class ListDetailsShowItemView : ListDetailsItemView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_list_details_show_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)

    imageLoadCompleteListener = {
      if (item.translation == null) {
        missingTranslationListener?.invoke(item)
      }
    }
//    watchlistMoviesRoot.onClick { itemClickListener?.invoke(item) }
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
    loadImage(item)
  }
}
