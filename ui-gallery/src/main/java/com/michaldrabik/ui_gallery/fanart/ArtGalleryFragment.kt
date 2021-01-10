package com.michaldrabik.ui_gallery.fanart

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_USER
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.colorStateListFromAttr
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.nextPage
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.updateTopMargin
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_gallery.R
import com.michaldrabik.ui_gallery.fanart.di.UiArtGalleryComponentProvider
import com.michaldrabik.ui_gallery.fanart.recycler.ArtGalleryAdapter
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.ImageFamily
import com.michaldrabik.ui_model.ImageFamily.SHOW
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_FAMILY
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_PICK_MODE
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_TYPE
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_CUSTOM_IMAGE
import kotlinx.android.synthetic.main.fragment_art_gallery.*

@SuppressLint("SetTextI18n", "DefaultLocale", "SourceLockedOrientationActivity")
class ArtGalleryFragment : BaseFragment<ArtGalleryViewModel>(R.layout.fragment_art_gallery) {

  override val viewModel by viewModels<ArtGalleryViewModel> { viewModelFactory }

  private val showId by lazy { IdTrakt(arguments?.getLong(ARG_SHOW_ID, -1) ?: -1) }
  private val movieId by lazy { IdTrakt(arguments?.getLong(ARG_MOVIE_ID, -1) ?: -1) }
  private val family by lazy { arguments?.getSerializable(ARG_FAMILY) as ImageFamily }
  private val type by lazy { arguments?.getSerializable(ARG_TYPE) as ImageType }
  private val isPickMode by lazy { arguments?.getBoolean(ARG_PICK_MODE, false) }

  private val galleryAdapter by lazy { ArtGalleryAdapter() }

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiArtGalleryComponentProvider).provideArtGalleryComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    if (type != POSTER) {
      requireActivity().requestedOrientation = SCREEN_ORIENTATION_FULL_USER
    }
    setupView()
    setupStatusBar()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
      val id = if (family == SHOW) showId else movieId
      loadImages(id, family, type)
    }
  }

  override fun onResume() {
    super.onResume()
    handleBackPressed()
  }

  override fun onDestroyView() {
    requireActivity().requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
    super.onDestroyView()
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    when (newConfig.orientation) {
      ORIENTATION_LANDSCAPE -> {
        val color = requireContext().colorStateListFromAttr(R.attr.textColorOnSurface)
        artGallerySelectButton.setTextColor(color)
        artGalleryBackArrow.imageTintList = color
        artGalleryPagerIndicatorWhite.visible()
        artGalleryPagerIndicator.gone()
        artGalleryPagerIndicatorWhite.setViewPager(artGalleryPager)
      }
      ORIENTATION_PORTRAIT -> {
        val color = requireContext().colorStateListFromAttr(android.R.attr.textColorPrimary)
        artGallerySelectButton.setTextColor(color)
        artGalleryBackArrow.imageTintList = color
        artGalleryPagerIndicatorWhite.gone()
        artGalleryPagerIndicator.visible()
        artGalleryPagerIndicator.setViewPager(artGalleryPager)
      }
    }
  }

  private fun setupView() {
    artGalleryBackArrow.onClick {
      if (isPickMode == true) setFragmentResult(REQUEST_CUSTOM_IMAGE, bundleOf())
      requireActivity().onBackPressed()
    }
    artGalleryPager.run {
      galleryAdapter.onItemClickListener = { nextPage() }
      adapter = galleryAdapter
      offscreenPageLimit = 2
      artGalleryPagerIndicator.setViewPager(this)
      adapter?.registerAdapterDataObserver(artGalleryPagerIndicator.adapterDataObserver)
    }
    artGallerySelectButton.run {
      visibleIf(isPickMode == true)
      onClick {
        val id = if (family == SHOW) showId else movieId
        val currentImage = galleryAdapter.getItem(artGalleryPager.currentItem)
        viewModel.saveCustomImage(id, currentImage, family, type)
      }
    }
  }

  private fun setupStatusBar() {
    artGalleryBackArrow.doOnApplyWindowInsets { view, insets, _, _ ->
      view.updateTopMargin(insets.systemWindowInsetTop)
      artGallerySelectButton.updateTopMargin(insets.systemWindowInsetTop)
    }
  }

  private fun render(uiModel: ArtGalleryUiModel) {
    uiModel.run {
      images?.let {
        galleryAdapter.setItems(it, type!!)
      }
      pickedImage?.let {
        it.consume()?.let {
          setFragmentResult(REQUEST_CUSTOM_IMAGE, bundleOf())
          requireActivity().onBackPressed()
        }
      }
    }
  }

  private fun handleBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      if (isPickMode == true) setFragmentResult(REQUEST_CUSTOM_IMAGE, bundleOf())
      remove()
      findNavControl().popBackStack()
    }
  }
}
