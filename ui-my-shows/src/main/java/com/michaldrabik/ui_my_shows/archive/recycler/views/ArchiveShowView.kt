package com.michaldrabik.ui_my_shows.archive.recycler.views

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
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.archive.recycler.ArchiveListItem
import kotlinx.android.synthetic.main.view_archive_show.view.*
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n")
class ArchiveShowView : ShowView<ArchiveListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_archive_show, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    archiveShowRoot.onClick { itemClickListener?.invoke(item) }
    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = archiveShowImage
  override val placeholderView: ImageView = archiveShowPlaceholder

  private lateinit var item: ArchiveListItem

  override fun bind(item: ArchiveListItem) {
    clear()
    this.item = item
    archiveShowProgress.visibleIf(item.isLoading)
    archiveShowTitle.text =
      if (item.translation?.title.isNullOrBlank()) item.show.title
      else item.translation?.title

    archiveShowDescription.text =
      if (item.translation?.overview.isNullOrBlank()) item.show.overview
      else item.translation?.overview

    archiveShowNetwork.text =
      if (item.show.year > 0) context.getString(R.string.textNetwork, item.show.network, item.show.year.toString())
      else String.format("%s", item.show.network)

    archiveShowRating.text = String.format(ENGLISH, "%.1f", item.show.rating)
    archiveShowDescription.visibleIf(item.show.overview.isNotBlank())
    archiveShowNetwork.visibleIf(item.show.network.isNotBlank())

    item.userRating?.let {
      archiveShowUserStarIcon.visible()
      archiveShowUserRating.visible()
      archiveShowUserRating.text = String.format(ENGLISH, "%d", it)
    }

    loadImage(item)
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    archiveShowTitle.text = ""
    archiveShowDescription.text = ""
    archiveShowNetwork.text = ""
    archiveShowRating.text = ""
    archiveShowPlaceholder.gone()
    archiveShowUserRating.gone()
    archiveShowUserStarIcon.gone()
    Glide.with(this).clear(archiveShowImage)
  }
}
