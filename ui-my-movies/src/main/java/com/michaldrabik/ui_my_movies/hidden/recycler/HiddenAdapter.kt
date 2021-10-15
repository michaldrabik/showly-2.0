package com.michaldrabik.ui_my_movies.hidden.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.BaseMovieAdapter
import com.michaldrabik.ui_my_movies.hidden.recycler.views.HiddenMovieView

class HiddenAdapter(
  itemClickListener: (HiddenListItem) -> Unit,
  missingImageListener: (HiddenListItem, Boolean) -> Unit,
  missingTranslationListener: (HiddenListItem) -> Unit,
  listChangeListener: () -> Unit,
) : BaseMovieAdapter<HiddenListItem>(
  itemClickListener = itemClickListener,
  missingImageListener = missingImageListener,
  missingTranslationListener = missingTranslationListener,
  listChangeListener = listChangeListener
) {

  init {
    stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
  }

  override val asyncDiffer = AsyncListDiffer(this, HiddenDiffCallback())

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    BaseViewHolder(
      HiddenMovieView(parent.context).apply {
        itemClickListener = { super.itemClickListener?.invoke(it) }
        missingImageListener = { item, force -> super.missingImageListener?.invoke(item, force) }
        missingTranslationListener = { super.missingTranslationListener?.invoke(it) }
      }
    )

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = asyncDiffer.currentList[position]
    (holder.itemView as HiddenMovieView).bind(item)
  }
}
