package com.michaldrabik.ui_people.details.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_people.details.recycler.views.PersonDetailsBioView
import com.michaldrabik.ui_people.details.recycler.views.PersonDetailsCreditsItemView
import com.michaldrabik.ui_people.details.recycler.views.PersonDetailsFiltersView
import com.michaldrabik.ui_people.details.recycler.views.PersonDetailsHeaderView
import com.michaldrabik.ui_people.details.recycler.views.PersonDetailsInfoView
import com.michaldrabik.ui_people.details.recycler.views.PersonDetailsLoadingView

class PersonDetailsAdapter(
  var onItemClickListener: (PersonDetailsItem) -> Unit,
  val onImageMissingListener: (PersonDetailsItem, Boolean) -> Unit,
  val onTranslationMissingListener: (PersonDetailsItem) -> Unit,
  val onLinksClickListener: (Person) -> Unit,
  val onImageClickListener: () -> Unit,
  var onFiltersChangeListener: ((List<Mode>) -> Unit)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  companion object {
    private const val VIEW_TYPE_INFO = 1
    private const val VIEW_TYPE_BIO = 2
    private const val VIEW_TYPE_CREDIT_ITEM = 3
    private const val VIEW_TYPE_CREDIT_HEADER = 4
    private const val VIEW_TYPE_CREDIT_LOADING = 5
    private const val VIEW_TYPE_CREDIT_FILTERS = 6
  }

  private val asyncDiffer = AsyncListDiffer(this, PersonDetailsItemDiffCallback())

  fun setItems(items: List<PersonDetailsItem>) = asyncDiffer.submitList(items)

  override fun getItemViewType(position: Int) =
    when (asyncDiffer.currentList[position]) {
      is PersonDetailsItem.MainInfo -> VIEW_TYPE_INFO
      is PersonDetailsItem.MainBio -> VIEW_TYPE_BIO
      is PersonDetailsItem.CreditsShowItem -> VIEW_TYPE_CREDIT_ITEM
      is PersonDetailsItem.CreditsMovieItem -> VIEW_TYPE_CREDIT_ITEM
      is PersonDetailsItem.CreditsHeader -> VIEW_TYPE_CREDIT_HEADER
      is PersonDetailsItem.CreditsLoadingItem -> VIEW_TYPE_CREDIT_LOADING
      is PersonDetailsItem.CreditsFiltersItem -> VIEW_TYPE_CREDIT_FILTERS
      else -> throw IllegalStateException()
    }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    when (viewType) {
      VIEW_TYPE_INFO -> BaseViewHolder(
        PersonDetailsInfoView(parent.context).apply {
          onLinksClickListener = this@PersonDetailsAdapter.onLinksClickListener
          onImageClickListener = this@PersonDetailsAdapter.onImageClickListener
        }
      )
      VIEW_TYPE_BIO -> BaseViewHolder(PersonDetailsBioView(parent.context))
      VIEW_TYPE_CREDIT_ITEM -> BaseViewHolder(
        PersonDetailsCreditsItemView(parent.context).apply {
          onItemClickListener = this@PersonDetailsAdapter.onItemClickListener
          onImageMissingListener = this@PersonDetailsAdapter.onImageMissingListener
          onTranslationMissingListener = this@PersonDetailsAdapter.onTranslationMissingListener
        }
      )
      VIEW_TYPE_CREDIT_HEADER -> BaseViewHolder(PersonDetailsHeaderView(parent.context))
      VIEW_TYPE_CREDIT_LOADING -> BaseViewHolder(PersonDetailsLoadingView(parent.context))
      VIEW_TYPE_CREDIT_FILTERS -> BaseViewHolder(
        PersonDetailsFiltersView(parent.context).apply {
          onChipsChangeListener = this@PersonDetailsAdapter.onFiltersChangeListener
        }
      )
      else -> throw IllegalStateException()
    }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
    when (val item = asyncDiffer.currentList[position]) {
      is PersonDetailsItem.MainInfo -> (holder.itemView as PersonDetailsInfoView).bind(item)
      is PersonDetailsItem.MainBio -> (holder.itemView as PersonDetailsBioView).bind(item)
      is PersonDetailsItem.CreditsHeader -> (holder.itemView as PersonDetailsHeaderView).bind(item)
      is PersonDetailsItem.CreditsShowItem -> (holder.itemView as PersonDetailsCreditsItemView).bind(item)
      is PersonDetailsItem.CreditsMovieItem -> (holder.itemView as PersonDetailsCreditsItemView).bind(item)
      is PersonDetailsItem.CreditsFiltersItem -> (holder.itemView as PersonDetailsFiltersView).bind(item.filters)
      is PersonDetailsItem.CreditsLoadingItem -> Unit
    }

  override fun getItemCount() = asyncDiffer.currentList.size

  class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
