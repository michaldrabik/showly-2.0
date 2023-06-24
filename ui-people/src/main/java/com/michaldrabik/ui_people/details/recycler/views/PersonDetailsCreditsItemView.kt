package com.michaldrabik.ui_people.details.recycler.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.michaldrabik.common.Config
import com.michaldrabik.common.Config.SPOILERS_HIDE_SYMBOL
import com.michaldrabik.common.Config.SPOILERS_REGEX
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_model.ImageStatus
import com.michaldrabik.ui_people.R
import com.michaldrabik.ui_people.details.recycler.PersonDetailsItem
import kotlinx.android.synthetic.main.view_person_details_credits_item.view.viewPersonCreditsItemBadge
import kotlinx.android.synthetic.main.view_person_details_credits_item.view.viewPersonCreditsItemDescription
import kotlinx.android.synthetic.main.view_person_details_credits_item.view.viewPersonCreditsItemIcon
import kotlinx.android.synthetic.main.view_person_details_credits_item.view.viewPersonCreditsItemImage
import kotlinx.android.synthetic.main.view_person_details_credits_item.view.viewPersonCreditsItemNetwork
import kotlinx.android.synthetic.main.view_person_details_credits_item.view.viewPersonCreditsItemPlaceholder
import kotlinx.android.synthetic.main.view_person_details_credits_item.view.viewPersonCreditsItemRoot
import kotlinx.android.synthetic.main.view_person_details_credits_item.view.viewPersonCreditsItemTitle
import kotlinx.android.synthetic.main.view_person_details_credits_item.view.viewPersonCreditsItemWatchlistBadge

class PersonDetailsCreditsItemView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onItemClickListener: ((PersonDetailsItem) -> Unit)? = null
  var onImageMissingListener: ((PersonDetailsItem, Boolean) -> Unit)? = null
  var onTranslationMissingListener: ((PersonDetailsItem) -> Unit)? = null

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.mediaTileCorner) }
  private val spaceNano by lazy { context.dimenToPx(R.dimen.spaceNano).toFloat() }
  private val centerCropTransformation by lazy { CenterCrop() }
  private val cornersTransformation by lazy { RoundedCorners(cornerRadius) }

  private lateinit var item: PersonDetailsItem

  init {
    inflate(context, R.layout.view_person_details_credits_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    viewPersonCreditsItemRoot.onClick { onItemClickListener?.invoke(item) }
  }

  fun bind(item: PersonDetailsItem.CreditsShowItem) {
    clear()
    this.item = item
    bindTitleDescription(item)

    val year = if (item.show.year > 0) item.show.year.toString() else "TBA"
    viewPersonCreditsItemNetwork.text =
      if (item.show.network.isNotBlank()) context.getString(R.string.textNetwork, year, item.show.network)
      else String.format("%s", year)

    viewPersonCreditsItemPlaceholder.setImageResource(R.drawable.ic_television)
    viewPersonCreditsItemIcon.setImageResource(R.drawable.ic_television)
    viewPersonCreditsItemNetwork.translationY = spaceNano
    viewPersonCreditsItemBadge.visibleIf(item.isMy)
    viewPersonCreditsItemWatchlistBadge.visibleIf(item.isWatchlist)

    if (!item.isLoading) loadImage(item)
  }

  fun bind(item: PersonDetailsItem.CreditsMovieItem) {
    clear()
    this.item = item
    bindTitleDescription(item)
    viewPersonCreditsItemNetwork.text = String.format("%s", item.movie.released?.year ?: "TBA")

    viewPersonCreditsItemPlaceholder.setImageResource(R.drawable.ic_film)
    viewPersonCreditsItemIcon.setImageResource(R.drawable.ic_film)
    viewPersonCreditsItemNetwork.translationY = 0F
    viewPersonCreditsItemRoot.alpha = if (item.moviesEnabled) 1F else 0.45F
    viewPersonCreditsItemRoot.isEnabled = item.moviesEnabled
    viewPersonCreditsItemBadge.visibleIf(item.isMy)
    viewPersonCreditsItemWatchlistBadge.visibleIf(item.isWatchlist)

    if (!item.isLoading) loadImage(item)
  }

  private fun bindTitleDescription(item: PersonDetailsItem.CreditsShowItem) {
    viewPersonCreditsItemTitle.text = when {
      item.translation?.title?.isNotBlank() == true -> item.translation.title
      else -> item.show.title
    }
    var description = when {
      item.translation?.overview?.isNotBlank() == true -> item.translation.overview
      item.show.overview.isNotBlank() -> item.show.overview
      else -> context.getString(R.string.textNoDescription)
    }

    val isMyHidden = item.spoilers.isMyShowsHidden && item.isMy
    val isWatchlistHidden = item.spoilers.isWatchlistShowsHidden && item.isWatchlist
    val isNotCollectedHidden = item.spoilers.isNotCollectedShowsHidden && (!item.isMy && !item.isWatchlist)
    if (isMyHidden || isWatchlistHidden || isNotCollectedHidden) {
      viewPersonCreditsItemDescription.tag = description
      description = SPOILERS_REGEX.replace(description, SPOILERS_HIDE_SYMBOL)

      if (item.spoilers.isTapToReveal) {
        viewPersonCreditsItemDescription.onClick { view ->
          view.tag?.let {
            viewPersonCreditsItemDescription.text = it.toString()
          }
          view.isClickable = false
        }
      }
    }

    viewPersonCreditsItemDescription.text = description
  }

  private fun bindTitleDescription(item: PersonDetailsItem.CreditsMovieItem) {
    viewPersonCreditsItemTitle.text = when {
      item.translation?.title?.isNotBlank() == true -> item.translation.title
      else -> item.movie.title
    }

    var description = when {
      item.translation?.overview?.isNotBlank() == true -> item.translation.overview
      item.movie.overview.isNotBlank() -> item.movie.overview
      else -> context.getString(R.string.textNoDescription)
    }

    val isMyHidden = item.spoilers.isMyMoviesHidden && item.isMy
    val isWatchlistHidden = item.spoilers.isWatchlistMoviesHidden && item.isWatchlist
    val isNotCollectedHidden = item.spoilers.isNotCollectedMoviesHidden && (!item.isMy && !item.isWatchlist)
    if (isMyHidden || isWatchlistHidden || isNotCollectedHidden) {
      viewPersonCreditsItemDescription.tag = description
      description = SPOILERS_REGEX.replace(description, SPOILERS_HIDE_SYMBOL)

      if (item.spoilers.isTapToReveal) {
        viewPersonCreditsItemDescription.onClick { view ->
          view.tag?.let {
            viewPersonCreditsItemDescription.text = it.toString()
          }
          view.isClickable = false
        }
      }
    }

    viewPersonCreditsItemDescription.text = description
  }

  private fun loadImage(item: PersonDetailsItem) {
    val image = when (item) {
      is PersonDetailsItem.CreditsShowItem -> item.image
      is PersonDetailsItem.CreditsMovieItem -> item.image
      else -> throw IllegalArgumentException()
    }

    if (image.status == ImageStatus.UNAVAILABLE) {
      viewPersonCreditsItemImage.gone()
      viewPersonCreditsItemPlaceholder.fadeIn(Config.IMAGE_FADE_DURATION_MS.toLong())
      return
    }

    if (image.status == ImageStatus.UNKNOWN) {
      onImageMissingListener?.invoke(item, true)
      return
    }

    Glide.with(this)
      .load(image.fullFileUrl)
      .transform(centerCropTransformation, cornersTransformation)
      .transition(DrawableTransitionOptions.withCrossFade(Config.IMAGE_FADE_DURATION_MS))
      .withSuccessListener {
        viewPersonCreditsItemPlaceholder.gone()
        loadTranslation(item)
      }
      .withFailListener {
        if (image.status == ImageStatus.AVAILABLE) {
          viewPersonCreditsItemImage.gone()
          viewPersonCreditsItemPlaceholder.fadeIn(Config.IMAGE_FADE_DURATION_MS.toLong())
          loadTranslation(item)
          return@withFailListener
        }
        onImageMissingListener?.invoke(item, false)
      }
      .into(viewPersonCreditsItemImage)
  }

  private fun loadTranslation(item: PersonDetailsItem) {
    if (item is PersonDetailsItem.CreditsShowItem && item.translation == null) {
      onTranslationMissingListener?.invoke(item)
    }
    if (item is PersonDetailsItem.CreditsMovieItem && item.translation == null) {
      onTranslationMissingListener?.invoke(item)
    }
  }

  private fun clear() {
    viewPersonCreditsItemBadge.gone()
    viewPersonCreditsItemWatchlistBadge.gone()
    viewPersonCreditsItemRoot.alpha = 1F
    viewPersonCreditsItemRoot.isEnabled = true
    viewPersonCreditsItemPlaceholder.gone()
    viewPersonCreditsItemImage.visible()
    Glide.with(this).clear(viewPersonCreditsItemImage)
  }
}
