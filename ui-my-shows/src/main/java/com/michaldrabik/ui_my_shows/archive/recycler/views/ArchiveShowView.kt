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
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.archive.recycler.ArchiveListItem
import kotlinx.android.synthetic.main.view_archive_show.view.*
import java.util.Locale.ROOT

@SuppressLint("SetTextI18n")
class ArchiveShowView : ShowView<ArchiveListItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_archive_show, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    archiveShowRoot.onClick { itemClickListener?.invoke(item) }
  }

  override val imageView: ImageView = archiveShowImage
  override val placeholderView: ImageView = archiveShowPlaceholder

  private lateinit var item: ArchiveListItem

  override fun bind(
    item: ArchiveListItem,
    missingImageListener: (ArchiveListItem, Boolean) -> Unit
  ) {
    clear()
    this.item = item
    archiveShowProgress.visibleIf(item.isLoading)
    archiveShowTitle.text = item.show.title
    archiveShowDescription.text =
      if (item.translation?.overview.isNullOrBlank()) item.show.overview
      else item.translation?.overview

    val year = if (item.show.year > 0) " (${item.show.year})" else ""
    archiveShowNetwork.text = "${item.show.network}$year"
    archiveShowRating.text = String.format(ROOT, "%.1f", item.show.rating)
    archiveShowDescription.visibleIf(item.show.overview.isNotBlank())
    archiveShowNetwork.visibleIf(item.show.network.isNotBlank())

    loadImage(item, missingImageListener)
  }

  private fun clear() {
    archiveShowTitle.text = ""
    archiveShowDescription.text = ""
    archiveShowNetwork.text = ""
    archiveShowRating.text = ""
    archiveShowPlaceholder.gone()
    Glide.with(this).clear(archiveShowImage)
  }
}
