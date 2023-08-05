package com.michaldrabik.ui_my_shows.myshows.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.databinding.ViewCollectionShowCompactBinding
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class MyShowAllCompactView : ShowView<MyShowsItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewCollectionShowCompactBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    with(binding) {
      collectionShowRoot.onClick { itemClickListener?.invoke(item) }
      collectionShowRoot.onLongClick { itemLongClickListener?.invoke(item) }
    }
    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = binding.collectionShowImage
  override val placeholderView: ImageView = binding.collectionShowPlaceholder

  private lateinit var item: MyShowsItem

  override fun bind(item: MyShowsItem) {
    clear()
    this.item = item

    with(binding) {
      collectionShowProgress.visibleIf(item.isLoading)
      collectionShowTitle.text =
        if (item.translation?.title.isNullOrBlank()) item.show.title
        else item.translation?.title

      collectionShowNetwork.text =
        if (item.show.year > 0) context.getString(R.string.textNetwork, item.show.network, item.show.year.toString())
        else String.format("%s", item.show.network)

      bindRating(item)
      collectionShowNetwork.visibleIf(item.show.network.isNotBlank())

      item.userRating?.let {
        collectionShowUserStarIcon.visible()
        collectionShowUserRating.visible()
        collectionShowUserRating.text = String.format(ENGLISH, "%d", it)
      }
    }

    loadImage(item)
  }

  private fun bindRating(item: MyShowsItem) {
    var rating = String.format(ENGLISH, "%.1f", item.show.rating)
    with(binding) {
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
      collectionShowTitle.text = ""
      collectionShowNetwork.text = ""
      collectionShowRating.text = ""
      collectionShowPlaceholder.gone()
      collectionShowUserStarIcon.gone()
      collectionShowUserRating.gone()
      Glide.with(this@MyShowAllCompactView).clear(collectionShowImage)
    }
  }
}
