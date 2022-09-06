package com.michaldrabik.ui_discover.filters.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.view.children
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_discover.R
import com.michaldrabik.ui_discover.databinding.ViewDiscoverFiltersBinding
import com.michaldrabik.ui_model.DiscoverFilters
import com.michaldrabik.ui_model.DiscoverSortOrder
import com.michaldrabik.ui_model.DiscoverSortOrder.HOT
import com.michaldrabik.ui_model.DiscoverSortOrder.NEWEST
import com.michaldrabik.ui_model.DiscoverSortOrder.RATING
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.Network

class DiscoverFiltersView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewDiscoverFiltersBinding.inflate(LayoutInflater.from(context), this)

  var onFeedChipClick: (() -> Unit)? = null
  var onGenresChipClick: (() -> Unit)? = null
  var onNetworksChipClick: (() -> Unit)? = null
  var onHideCollectionChipClick: (() -> Unit)? = null
  var onHideAnticipatedChipClick: (() -> Unit)? = null

  private lateinit var filters: DiscoverFilters

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    with(binding) {
      discoverGenresChip.text = discoverGenresChip.text.toString().filter { it.isLetter() }
      discoverGenresChip.onClick { onGenresChipClick?.invoke() }
      discoverNetworksChip.text = discoverNetworksChip.text.toString().filter { it.isLetter() }
      discoverNetworksChip.onClick { onNetworksChipClick?.invoke() }
      discoverFeedChip.isSelected = true
      discoverFeedChip.onClick { onFeedChipClick?.invoke() }
      discoverCollectionChip.onClick { onHideCollectionChipClick?.invoke() }
      discoverAnticipatedChip.onClick { onHideAnticipatedChipClick?.invoke() }
    }
  }

  fun bind(filters: DiscoverFilters) {
    this.filters = filters
    bindFeed(filters.feedOrder)
    bindGenres(filters.genres)
    bindNetworks(filters.networks)
    with(binding) {
      discoverCollectionChip.isChecked = filters.hideCollection
      discoverAnticipatedChip.isChecked = filters.hideAnticipated
    }
  }

  private fun bindFeed(feed: DiscoverSortOrder) {
    with(binding) {
      discoverFeedChip.text = when (feed) {
        HOT -> context.getString(R.string.textHot)
        RATING -> context.getString(R.string.textSortRated)
        NEWEST -> context.getString(R.string.textSortNewest)
      }
    }
  }

  private fun bindGenres(genres: List<Genre>) {
    with(binding) {
      discoverGenresChip.isSelected = genres.isNotEmpty()
      discoverGenresChip.text = when {
        genres.isEmpty() -> context.getString(R.string.textGenres).filter { it.isLetter() }
        genres.size == 1 -> context.getString(genres.first().displayName)
        genres.size == 2 -> "${context.getString(genres[0].displayName)}, ${context.getString(genres[1].displayName)}"
        else -> "${context.getString(genres[0].displayName)}, ${context.getString(genres[1].displayName)} + ${genres.size - 2}"
      }
    }
  }

  private fun bindNetworks(networks: List<Network>) {
    with(binding) {
      discoverNetworksChip.isSelected = networks.isNotEmpty()
      discoverNetworksChip.text = when {
        networks.isEmpty() -> context.getString(R.string.textNetworks).filter { it.isLetter() }
        networks.size == 1 -> networks[0].channels.first()
        else -> throw IllegalStateException()
      }
    }
  }

  override fun setEnabled(enabled: Boolean) {
    binding.discoverChips.children.forEach { it.isEnabled = enabled }
  }
}
