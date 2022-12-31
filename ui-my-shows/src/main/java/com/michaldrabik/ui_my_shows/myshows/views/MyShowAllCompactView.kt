package com.michaldrabik.ui_my_shows.myshows.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import kotlinx.android.synthetic.main.view_collection_show_compact.view.*
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class MyShowAllCompactView : ShowView<MyShowsItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_collection_show_compact, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    collectionShowRoot.onClick { itemClickListener?.invoke(item) }
    collectionShowRoot.onLongClick { itemLongClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = collectionShowImage
  override val placeholderView: ImageView = collectionShowPlaceholder

  private lateinit var item: MyShowsItem

  override fun bind(item: MyShowsItem) {
    clear()
    this.item = item
    collectionShowProgress.visibleIf(item.isLoading)
    collectionShowTitle.text =
      if (item.translation?.title.isNullOrBlank()) item.show.title
      else item.translation?.title

    collectionShowNetwork.text =
      if (item.show.year > 0) context.getString(R.string.textNetwork, item.show.network, item.show.year.toString())
      else String.format("%s", item.show.network)

    collectionShowRating.text = String.format(ENGLISH, "%.1f", item.show.rating)
    collectionShowNetwork.visibleIf(item.show.network.isNotBlank())

    item.userRating?.let {
      collectionShowUserStarIcon.visible()
      collectionShowUserRating.visible()
      collectionShowUserRating.text = String.format(ENGLISH, "%d", it)
    }

    loadImage(item)
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    collectionShowTitle.text = ""
    collectionShowNetwork.text = ""
    collectionShowRating.text = ""
    collectionShowPlaceholder.gone()
    collectionShowUserStarIcon.gone()
    collectionShowUserRating.gone()
    Glide.with(this).clear(collectionShowImage)
  }
}
