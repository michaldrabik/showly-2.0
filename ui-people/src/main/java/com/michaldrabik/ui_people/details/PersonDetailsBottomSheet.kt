package com.michaldrabik.ui_people.details

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.common.FastLinearLayoutManager
import com.michaldrabik.ui_base.utilities.TipsHost
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.requireParcelable
import com.michaldrabik.ui_base.utilities.extensions.screenHeight
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.Tip
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_PERSON
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_PERSON_DETAILS
import com.michaldrabik.ui_people.R
import com.michaldrabik.ui_people.databinding.ViewPersonDetailsBinding
import com.michaldrabik.ui_people.details.links.PersonLinksBottomSheet
import com.michaldrabik.ui_people.details.recycler.PersonDetailsAdapter
import com.michaldrabik.ui_people.details.recycler.PersonDetailsItem
import com.michaldrabik.ui_people.gallery.PersonGalleryFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersonDetailsBottomSheet : BaseBottomSheetFragment(R.layout.view_person_details) {

  companion object {
    const val SHOW_BACK_UP_BUTTON_THRESHOLD = 25

    fun createBundle(person: Person, sourceId: IdTrakt) =
      bundleOf(
        ARG_PERSON to person,
        ARG_ID to sourceId
      )
  }

  private val viewModel by viewModels<PersonDetailsViewModel>()
  private val binding by viewBinding(ViewPersonDetailsBinding::bind)

  private val person by lazy { requireParcelable<Person>(ARG_PERSON) }
  private val sourceId by lazy { requireParcelable<IdTrakt>(ARG_ID) }

  private var adapter: PersonDetailsAdapter? = null
  private var layoutManager: LinearLayoutManager? = null

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    setupView()
    setupTips()
    setupRecycler()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.messageFlow.collect { renderSnackbar(it) } },
      doAfterLaunch = { viewModel.loadDetails(person) }
    )
  }

  private fun setupView() {
    with(binding) {
      val behavior: BottomSheetBehavior<*> = (dialog as BottomSheetDialog).behavior
      with(behavior) {
        peekHeight = (screenHeight() * 0.55).toInt()
        skipCollapsed = true
        state = BottomSheetBehavior.STATE_COLLAPSED
      }
      personDetailsRecyclerFab.onClick {
        personDetailsRecyclerFab.fadeOut(150)
        personDetailsRecycler.smoothScrollToPosition(0)
      }
    }
  }

  private fun setupTips() {
    val isShown = (requireActivity() as TipsHost).isTipShown(Tip.PERSON_DETAILS_GALLERY)
    if (!isShown && !person.imagePath.isNullOrBlank()) {
      val message = getString(Tip.PERSON_DETAILS_GALLERY.textResId)
      binding.personDetailsSnackHost.showInfoSnackbar(message, length = Snackbar.LENGTH_INDEFINITE) {
        (requireActivity() as TipsHost).setTipShow(Tip.PERSON_DETAILS_GALLERY)
      }
    }
  }

  private fun setupRecycler() {
    layoutManager = FastLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    adapter = PersonDetailsAdapter(
      onItemClickListener = { openDetails(it) },
      onLinksClickListener = { openLinksSheet(it) },
      onImageClickListener = { openGallery() },
      onImageMissingListener = { item, force -> viewModel.loadMissingImage(item, force) },
      onTranslationMissingListener = { item -> viewModel.loadMissingTranslation(item) },
      onFiltersChangeListener = { filters -> viewModel.loadCredits(person, filters) }
    )
    with(binding.personDetailsRecycler) {
      adapter = this@PersonDetailsBottomSheet.adapter
      layoutManager = this@PersonDetailsBottomSheet.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      removeOnScrollListener(recyclerScrollListener)
      addOnScrollListener(recyclerScrollListener)
    }
  }

  private fun openDetails(item: PersonDetailsItem) {
    val personBundle = bundleOf(ARG_PERSON to person)
    if (item is PersonDetailsItem.CreditsShowItem && item.show.traktId != sourceId.id) {
      setFragmentResult(REQUEST_PERSON_DETAILS, personBundle)
      val bundle = bundleOf(NavigationArgs.ARG_SHOW_ID to item.show.traktId)
      requireParentFragment()
        .findNavController()
        .navigate(R.id.actionPersonDetailsDialogToShow, bundle)
    }
    if (item is PersonDetailsItem.CreditsMovieItem && item.movie.traktId != sourceId.id) {
      setFragmentResult(REQUEST_PERSON_DETAILS, personBundle)
      val bundle = bundleOf(NavigationArgs.ARG_MOVIE_ID to item.movie.traktId)
      requireParentFragment()
        .findNavController()
        .navigate(R.id.actionPersonDetailsDialogToMovie, bundle)
    }
  }

  private fun openGallery() {
    val personBundle = bundleOf(ARG_PERSON to person)
    setFragmentResult(REQUEST_PERSON_DETAILS, personBundle)
    val options = PersonGalleryFragment.createBundle(person)
    requireParentFragment()
      .findNavController()
      .navigate(R.id.actionPersonDetailsDialogToGallery, options)
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

  private fun renderSnackbar(message: MessageEvent) {
    when (message) {
      is MessageEvent.Info -> binding.viewPersonDetailsRoot.showInfoSnackbar(getString(message.textRestId))
      is MessageEvent.Error -> binding.viewPersonDetailsRoot.showErrorSnackbar(getString(message.textRestId))
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
        binding.personDetailsRecyclerFab.fadeIn(150)
      } else {
        binding.personDetailsRecyclerFab.fadeOut(150)
      }
    }
  }
}
