package com.michaldrabik.ui_people

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.common.FastLinearLayoutManager
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_PERSON
import com.michaldrabik.ui_people.links.PersonLinksBottomSheet
import com.michaldrabik.ui_people.recycler.PersonDetailsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.view_person_details.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class PersonDetailsBottomSheet : BaseBottomSheetFragment<PersonDetailsViewModel>() {

  companion object {
    const val SHOW_BACK_UP_BUTTON_THRESHOLD = 25

    fun createBundle(person: Person) = bundleOf(ARG_PERSON to person)
  }

  override val layoutResId = R.layout.view_person_details

  private val person by lazy { requireArguments().getParcelable<Person>(ARG_PERSON) as Person }

  private var adapter: PersonDetailsAdapter? = null
  private var layoutManager: LinearLayoutManager? = null

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    return inflater.cloneInContext(contextThemeWrapper).inflate(layoutResId, container, false)
  }

  override fun createViewModel() = ViewModelProvider(this)[PersonDetailsViewModel::class.java]

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    setupView()
    setupRecycler()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.loadDetails(person) }
    )
  }

  private fun setupView() {
    val behavior: BottomSheetBehavior<*> = (dialog as BottomSheetDialog).behavior
    with(behavior) {
      isFitToContents = false
      halfExpandedRatio = 0.55F
      skipCollapsed = true
      state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }
    personDetailsRecyclerFab.onClick {
      personDetailsRecyclerFab.fadeOut(150)
      personDetailsRecycler.smoothScrollToPosition(0)
    }
  }

  private fun setupRecycler() {
    layoutManager = FastLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    adapter = PersonDetailsAdapter(
      onLinksClickListener = { openLinksSheet(it) },
      onImageMissingListener = { item, force -> viewModel.loadMissingImage(item, force) },
      onTranslationMissingListener = { item -> viewModel.loadMissingTranslation(item) },
      onFiltersChangeListener = { filters -> viewModel.loadCredits(person, filters) }
    )
    with(personDetailsRecycler) {
      adapter = this@PersonDetailsBottomSheet.adapter
      layoutManager = this@PersonDetailsBottomSheet.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      removeOnScrollListener(recyclerScrollListener)
      addOnScrollListener(recyclerScrollListener)
    }
  }

  private fun openLinksSheet(it: Person) {
    val options = PersonLinksBottomSheet.createBundle(it)
    navigateTo(R.id.actionPersonDetailsDialogToLinks, options)
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: PersonDetailsUiState) {
    uiState.run {
      personDetailsItems?.let { adapter?.setItems(it) }
    }
  }

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }

  private val recyclerScrollListener = object : RecyclerView.OnScrollListener() {
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
      if (newState != RecyclerView.SCROLL_STATE_IDLE) return
      if ((layoutManager?.findFirstVisibleItemPosition() ?: 0) >= SHOW_BACK_UP_BUTTON_THRESHOLD) {
        personDetailsRecyclerFab.fadeIn(150)
      } else {
        personDetailsRecyclerFab.fadeOut(150)
      }
    }
  }
}
