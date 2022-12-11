package com.michaldrabik.ui_my_shows.common.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.common.recycler.CollectionListItem
import com.michaldrabik.ui_my_shows.databinding.ViewCollectionShowCompactBinding
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class CollectionShowCompactView : ShowView<CollectionListItem.ShowItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewCollectionShowCompactBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    binding.collectionShowRoot.onClick { itemClickListener?.invoke(item) }
    binding.collectionShowRoot.onLongClick { itemLongClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = binding.collectionShowImage
  override val placeholderView: ImageView = binding.collectionShowPlaceholder

  private var nowUtc = nowUtc()
  private lateinit var item: CollectionListItem.ShowItem

  override fun bind(item: CollectionListItem.ShowItem) {
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

      collectionShowRating.text = String.format(ENGLISH, "%.1f", item.show.rating)
      collectionShowNetwork.visibleIf(item.show.network.isNotBlank())

      with(collectionShowReleaseDate) {
        val releaseDate = item.getReleaseDate()
        if (releaseDate != null) {
          visibleIf(releaseDate.isAfter(nowUtc))
          text = item.dateFormat.format(releaseDate).capitalizeWords()
        } else {
          gone()
        }
      }

      item.userRating?.let {
        collectionShowUserStarIcon.visible()
        collectionShowUserRating.visible()
        collectionShowUserRating.text = String.format(ENGLISH, "%d", it)
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
      collectionShowTitle.text = ""
      collectionShowNetwork.text = ""
      collectionShowRating.text = ""
      collectionShowPlaceholder.gone()
      collectionShowUserRating.gone()
      collectionShowUserStarIcon.gone()
      Glide.with(this@CollectionShowCompactView).clear(collectionShowImage)
    }
  }
}
