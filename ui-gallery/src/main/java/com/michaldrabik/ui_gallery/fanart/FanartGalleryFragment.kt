package com.michaldrabik.ui_gallery.fanart

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_USER
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.updateMargins
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.colorStateListFromAttr
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.nextPage
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_gallery.R
import com.michaldrabik.ui_gallery.fanart.di.UiFanartGalleryComponentProvider
import com.michaldrabik.ui_gallery.fanart.recycler.FanartGalleryAdapter
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.ImageFamily
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_TYPE
import kotlinx.android.synthetic.main.fragment_fanart_gallery.*

@SuppressLint("SetTextI18n", "DefaultLocale", "SourceLockedOrientationActivity")
class FanartGalleryFragment : BaseFragment<FanartGalleryViewModel>(R.layout.fragment_fanart_gallery) {

  override val viewModel by viewModels<FanartGalleryViewModel> { viewModelFactory }

  private val showId by lazy { IdTrakt(arguments?.getLong(ARG_SHOW_ID, -1) ?: -1) }
  private val movieId by lazy { IdTrakt(arguments?.getLong(ARG_MOVIE_ID, -1) ?: -1) }
  private val type by lazy { arguments?.getSerializable(ARG_TYPE) as ImageFamily }
  private val galleryAdapter by lazy { FanartGalleryAdapter() }

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiFanartGalleryComponentProvider).provideFanartGalleryComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    requireActivity().requestedOrientation = SCREEN_ORIENTATION_FULL_USER
    setupView()
    setupStatusBar()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
      val id = if (showId.id != -1L) showId else movieId
      loadImage(id, type)
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
        fanartGalleryBackArrow.imageTintList = requireContext().colorStateListFromAttr(R.attr.textColorOnSurface)
        fanartGalleryPagerIndicatorWhite.visible()
        fanartGalleryPagerIndicator.gone()
        fanartGalleryPagerIndicatorWhite.setViewPager(fanartGalleryPager)
      }
      ORIENTATION_PORTRAIT -> {
        fanartGalleryBackArrow.imageTintList = requireContext().colorStateListFromAttr(android.R.attr.textColorPrimary)
        fanartGalleryPagerIndicatorWhite.gone()
        fanartGalleryPagerIndicator.visible()
        fanartGalleryPagerIndicator.setViewPager(fanartGalleryPager)
      }
    }
  }

  private fun setupView() {
    fanartGalleryBackArrow.onClick { requireActivity().onBackPressed() }
    fanartGalleryPager.run {
      galleryAdapter.onItemClickListener = { nextPage() }
      adapter = galleryAdapter
      offscreenPageLimit = 2
      fanartGalleryPagerIndicator.setViewPager(this)
      adapter?.registerAdapterDataObserver(fanartGalleryPagerIndicator.adapterDataObserver)
    }
  }

  private fun setupStatusBar() {
    fanartGalleryBackArrow.doOnApplyWindowInsets { view, insets, _, _ ->
      (view.layoutParams as ViewGroup.MarginLayoutParams).updateMargins(top = insets.systemWindowInsetTop)
    }
  }

  private fun render(uiModel: FanartGalleryUiModel) {
    uiModel.run {
      images?.let {
        galleryAdapter.setItems(it)
      }
    }
  }

  private fun handleBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      remove()
      findNavControl().popBackStack()
    }
  }
}
