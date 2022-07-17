package com.michaldrabik.ui_people.gallery

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.nextPage
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.openWebUrl
import com.michaldrabik.ui_base.utilities.extensions.requireParcelable
import com.michaldrabik.ui_base.utilities.extensions.updateTopMargin
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ID
import com.michaldrabik.ui_people.R
import com.michaldrabik.ui_people.gallery.recycler.PersonGalleryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_person_gallery.*

@AndroidEntryPoint
class PersonGalleryFragment : BaseFragment<PersonGalleryViewModel>(R.layout.fragment_person_gallery) {

  companion object {
    fun createBundle(person: Person): Bundle {
      return bundleOf(ARG_ID to person.ids.tmdb)
    }
  }

  override val viewModel by viewModels<PersonGalleryViewModel>()

  private val personId by lazy { requireParcelable<IdTmdb>(ARG_ID) }

  private var galleryAdapter: PersonGalleryAdapter? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupStatusBar()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      doAfterLaunch = { viewModel.loadImages(personId) }
    )
  }

  override fun onDestroyView() {
    galleryAdapter = null
    super.onDestroyView()
  }

  private fun setupView() {
    personGalleryBackArrow.onClick {
      requireActivity().onBackPressed()
    }
    personGalleryBrowserIcon.onClick {
      val currentIndex = personGalleryPager.currentItem
      val image = galleryAdapter?.getItem(currentIndex)
      openImageInBrowser(image?.fullFileUrl)
    }
    galleryAdapter = PersonGalleryAdapter(
      onItemClickListener = { personGalleryPager.nextPage() }
    )
    personGalleryPager.run {
      adapter = galleryAdapter
      offscreenPageLimit = 2
      personGalleryPagerIndicator.setViewPager(this)
      adapter?.registerAdapterDataObserver(personGalleryPagerIndicator.adapterDataObserver)
    }
  }

  private fun setupStatusBar() {
    requireView().doOnApplyWindowInsets { _, insets, _, _ ->
      val margin = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      personGalleryBackArrow.updateTopMargin(margin)
      personGalleryBrowserIcon.updateTopMargin(margin)
    }
  }

  private fun render(uiState: PersonGalleryUiState) {
    uiState.run {
      images?.let {
        galleryAdapter?.setItems(it)
        personGalleryEmptyView.visibleIf(it.isEmpty())
        personGalleryBrowserIcon.visibleIf(it.isNotEmpty())
      }
      isLoading.let {
        personGalleryImagesProgress.visibleIf(it)
      }
    }
  }

  private fun openImageInBrowser(url: String?) {
    url?.let { requireContext().openWebUrl(it) }
  }

  override fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      isEnabled = false
      findNavControl()?.popBackStack()
    }
  }
}
