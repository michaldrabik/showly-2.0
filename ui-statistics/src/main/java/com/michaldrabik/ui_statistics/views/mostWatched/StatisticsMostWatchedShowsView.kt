package com.michaldrabik.ui_statistics.views.mostWatched

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.ui_base.utilities.extensions.addDivider
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_statistics.R
import com.michaldrabik.ui_statistics.databinding.ViewStatisticsCardMostWatchedShowsBinding
import com.michaldrabik.ui_statistics.views.mostWatched.recycler.MostWatchedAdapter

@SuppressLint("SetTextI18n")
class StatisticsMostWatchedShowsView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewStatisticsCardMostWatchedShowsBinding.inflate(LayoutInflater.from(context), this)

  private var adapter: MostWatchedAdapter? = null
  private var layoutManager: LinearLayoutManager? = null

  var onLoadMoreClickListener: ((Int) -> Unit)? = null
  var onShowClickListener: ((Show) -> Unit)? = null

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    setupRecycler()
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    adapter = MostWatchedAdapter(
      itemClickListener = { onShowClickListener?.invoke(it.show) }
    )
    binding.viewMostWatchedShowsRecycler.apply {
      adapter = this@StatisticsMostWatchedShowsView.adapter
      layoutManager = this@StatisticsMostWatchedShowsView.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      addDivider(R.drawable.divider_statistics_most_watched)
    }
  }

  fun bind(
    items: List<StatisticsMostWatchedItem>,
    totalCount: Int
  ) {
    adapter?.setItems(items)
    binding.viewMostWatchedShowsMoreButton.run {
      visibleIf(items.size < totalCount)
      onClick { onLoadMoreClickListener?.invoke(10) }
    }
  }
}
