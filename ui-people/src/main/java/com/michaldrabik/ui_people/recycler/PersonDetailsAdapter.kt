package com.michaldrabik.ui_people.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_people.recycler.views.PersonDetailsBioView
import com.michaldrabik.ui_people.recycler.views.PersonDetailsInfoView

class PersonDetailsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  companion object {
    private const val VIEW_TYPE_INFO = 1
    private const val VIEW_TYPE_BIO = 2
  }

  private val asyncDiffer = AsyncListDiffer(this, PersonDetailsItemDiffCallback())

  fun setItems(items: List<PersonDetailsItem>) = asyncDiffer.submitList(items)

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position]) {
      is PersonDetailsItem.MainInfo -> VIEW_TYPE_INFO
      is PersonDetailsItem.MainBio -> VIEW_TYPE_BIO
      else -> throw IllegalStateException()
    }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
    VIEW_TYPE_INFO -> BaseViewHolder(PersonDetailsInfoView(parent.context))
    VIEW_TYPE_BIO -> BaseViewHolder(PersonDetailsBioView(parent.context))
    else -> throw IllegalStateException()
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (val item = asyncDiffer.currentList[position]) {
      is PersonDetailsItem.MainInfo -> (holder.itemView as PersonDetailsInfoView).bind(item)
      is PersonDetailsItem.MainBio -> (holder.itemView as PersonDetailsBioView).bind(item)
    }
  }

  override fun getItemCount() = asyncDiffer.currentList.size

  class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
