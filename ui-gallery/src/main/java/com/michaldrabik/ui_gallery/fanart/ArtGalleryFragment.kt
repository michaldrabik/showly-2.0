package com.michaldrabik.ui_gallery.fanart

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_USER
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.colorStateListFromAttr
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.nextPage
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.openWebUrl
import com.michaldrabik.ui_base.utilities.extensions.updateTopMargin
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_gallery.R
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_art_gallery.*
import kotlinx.android.synthetic.main.view_gallery_url_dialog.view.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

@SuppressLint("SetTextI18n", "DefaultLocale", "SourceLockedOrientationActivity")
@AndroidEntryPoint
class ArtGalleryFragment : BaseFragment<ArtGalleryViewModel>(R.layout.fragment_art_gallery) {

  companion object {
    private const val IMAGE_URL_PATTERN = "(http)?s?:?(//[^\"']*\\.(?:jpg|jpeg|png))"
  }

  override val viewModel by viewModels<ArtGalleryViewModel>()

  private val showId by lazy { IdTrakt(arguments?.getLong(ARG_SHOW_ID, -1) ?: -1) }
  private val movieId by lazy { IdTrakt(arguments?.getLong(ARG_MOVIE_ID, -1) ?: -1) }
  private val family by lazy { arguments?.getSerializable(ARG_FAMILY) as ImageFamily }
  private val type by lazy { arguments?.getSerializable(ARG_TYPE) as ImageType }
  private val isPickMode by lazy { arguments?.getBoolean(ARG_PICK_MODE, false) }

  private var galleryAdapter: ArtGalleryAdapter? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    if (type != POSTER) {
      requireActivity().requestedOrientation = SCREEN_ORIENTATION_FULL_USER
    }
    setupView()
    setupStatusBar()

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          val id = if (family == SHOW) showId else movieId
          loadImages(id, family, type)
        }
      }
    }
  }

  override fun onDestroyView() {
    galleryAdapter = null
    requireActivity().requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
    super.onDestroyView()
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    when (newConfig.orientation) {
      ORIENTATION_LANDSCAPE -> {
        val color = requireContext().colorStateListFromAttr(R.attr.textColorOnSurface)
        artGalleryBackArrow.imageTintList = color
        artGalleryPagerIndicatorWhite.visible()
        artGalleryPagerIndicator.gone()
        artGalleryPagerIndicatorWhite.setViewPager(artGalleryPager)
      }
      ORIENTATION_PORTRAIT -> {
        val color = requireContext().colorStateListFromAttr(android.R.attr.textColorPrimary)
        artGalleryBackArrow.imageTintList = color
        artGalleryPagerIndicatorWhite.gone()
        artGalleryPagerIndicator.visible()
        artGalleryPagerIndicator.setViewPager(artGalleryPager)
      }
      else -> Timber.d("Unused orientation")
    }
  }

  private fun setupView() {
    artGalleryBackArrow.onClick {
      if (isPickMode == true) setFragmentResult(REQUEST_CUSTOM_IMAGE, bundleOf())
      requireActivity().onBackPressed()
    }
    artGalleryBrowserIcon.onClick {
      val currentIndex = artGalleryPager.currentItem
      val image = galleryAdapter?.getItem(currentIndex)
      image?.fullFileUrl?.let { openWebUrl(it) }
    }
    galleryAdapter = ArtGalleryAdapter(
      onItemClickListener = { artGalleryPager.nextPage() }
    )
    artGalleryPager.run {
      adapter = galleryAdapter
      offscreenPageLimit = 2
      artGalleryPagerIndicator.setViewPager(this)
      adapter?.registerAdapterDataObserver(artGalleryPagerIndicator.adapterDataObserver)
    }
    artGallerySelectButton.run {
      onClick {
        val id = if (family == SHOW) showId else movieId
        val currentImage = galleryAdapter?.getItem(artGalleryPager.currentItem)
        currentImage?.let {
          viewModel.saveCustomImage(id, it, family, type)
        }
      }
    }
    artGalleryUrlButton.onClick { showUrlInput() }
  }

  private fun setupStatusBar() {
    requireView().doOnApplyWindowInsets { _, insets, _, _ ->
      val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      artGalleryBackArrow.updateTopMargin(inset)
      artGalleryBrowserIcon.updateTopMargin(inset)
      artGallerySelectButton.updateTopMargin(inset)
    }
  }

  private fun showUrlInput() {

    fun onUrlInput(view: View) {
      val input = view.urlDialogInput.text.toString()
      if (input.matches(IMAGE_URL_PATTERN.toRegex())) {
        artGalleryUrlProgress.visible()
        artGalleryUrlButton.gone()
        artGallerySelectButton.gone()
        Glide.with(requireContext())
          .load(input)
          .withSuccessListener {
            viewModel.addImageFromUrl(input, family, type)
            artGalleryUrlProgress.gone()
            artGalleryUrlButton.visible()
            artGallerySelectButton.visible()
          }
          .withFailListener {
            showSnack(MessageEvent.Error(R.string.textUrlDialogInvalidImage))
            artGalleryUrlProgress.gone()
            artGalleryUrlButton.visible()
            artGallerySelectButton.visible()
          }
          .preload(100, 100)
      } else {
        showSnack(MessageEvent.Error(R.string.textUrlDialogInvalidUrl))
      }
    }

    AlertDialog.Builder(requireContext(), R.style.UrlInputDialog).apply {
      val view = LayoutInflater.from(requireContext()).inflate(R.layout.view_gallery_url_dialog, fanartGalleryRoot, false)
      setView(view)
      setTitle(R.string.textUrlDialogTitle)
      setPositiveButton(R.string.textOk) { _, _ -> onUrlInput(view) }
      setNegativeButton(R.string.textCancel) { dialog, _ -> dialog.dismiss() }
      show()
    }
  }

  private fun render(uiState: ArtGalleryUiState) {
    uiState.run {
      images?.let {
        galleryAdapter?.setItems(it, type)
        artGalleryEmptyView.visibleIf(it.isEmpty())
        artGallerySelectButton.visibleIf(it.isNotEmpty() && isPickMode == true)
        artGalleryBrowserIcon.visibleIf(it.isNotEmpty() && isPickMode == false)
        artGalleryUrlButton.visibleIf(isPickMode == true)
      }
      pickedImage?.let {
        it.consume()?.let {
          setFragmentResult(REQUEST_CUSTOM_IMAGE, bundleOf())
          requireActivity().onBackPressed()
        }
      }
      artGalleryImagesProgress.visibleIf(isLoading)
    }
  }

  override fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      if (isPickMode == true) {
        setFragmentResult(REQUEST_CUSTOM_IMAGE, bundleOf())
      }
      isEnabled = false
      findNavControl()?.popBackStack()
    }
  }
}
