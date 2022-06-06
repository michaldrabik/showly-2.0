package com.michaldrabik.ui_show.quicksetup

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_show.databinding.ViewQuickSetupBinding

class QuickSetupView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewQuickSetupBinding.inflate(LayoutInflater.from(context), this)
  private val quickSetupAdapter by lazy { QuickSetupAdapter() }

  init {
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    setupRecycler()
  }

  private fun setupRecycler() {
    binding.viewQuickSetupRecycler.apply {
      setHasFixedSize(true)
      adapter = quickSetupAdapter
      layoutManager = LinearLayoutManager(context, VERTICAL, false)
      itemAnimator = null
    }
    quickSetupAdapter.onItemClickListener = { episode, isChecked ->
      onItemChecked(episode, isChecked)
    }
  }

  fun bind(seasons: List<Season>) {
    val items = mutableListOf<QuickSetupListItem>()
    seasons
      .filterNot { it.isSpecial() }
      .sortedByDescending { it.number }
      .forEach { season ->
        season.episodes
          .filter { it.hasAired(season) }
          .sortedByDescending { it.number }
          .forEachIndexed { index, episode ->
            if (index == 0) {
              items.add(QuickSetupListItem(episode, season, isHeader = true))
            }
            items.add(QuickSetupListItem(episode, season))
          }
      }
    quickSetupAdapter.setItems(items)
  }

  fun getSelectedItem() =
    quickSetupAdapter.getItems().firstOrNull { it.isChecked }

  private fun onItemChecked(episode: Episode, isChecked: Boolean) {
    val items = quickSetupAdapter.getItems()
      .map { it.copy(isChecked = false) }
      .toMutableList()

    val item = items
      .filterNot { it.isHeader }
      .first { it.episode.ids.trakt == episode.ids.trakt }
      .copy(isChecked = !isChecked)

    items.findReplace(item) { it.episode.ids.trakt == episode.ids.trakt && !it.isHeader }

    quickSetupAdapter.setItems(items.toList())
  }
}
