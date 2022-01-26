package com.michaldrabik.ui_my_movies.hidden.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_my_movies.hidden.recycler.views.HiddenMovieView

class HiddenAdapter(
  private val itemClickListener: (HiddenListItem) -> Unit,
  private val itemLongClickListener: (HiddenListItem) -> Unit,
  private val missingImageListener: (HiddenListItem, Boolean) -> Unit,
  private val missingTranslationListener: (HiddenListItem) -> Unit,
  listChangeListener: () -> Unit,
) : BaseMovieAdapter<HiddenListItem>(
  listChangeListener = listChangeListener
) {

  init {
    stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
  }

  override val asyncDiffer = AsyncListDiffer(this, HiddenDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    BaseViewHolder(
      HiddenMovieView(parent.context).apply {
        itemClickListener = this@HiddenAdapter.itemClickListener
        itemLongClickListener = this@HiddenAdapter.itemLongClickListener
        missingImageListener = this@HiddenAdapter.missingImageListener
        missingTranslationListener = this@HiddenAdapter.missingTranslationListener
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as HiddenMovieView).bind(item)
  }
}
