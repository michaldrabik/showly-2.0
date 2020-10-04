package com.michaldrabik.ui_show.gallery

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_USER
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.updateMargins
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.nextPage
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_show.R
import com.michaldrabik.ui_show.gallery.di.UiFanartGalleryComponentProvider
import com.michaldrabik.ui_show.gallery.recycler.FanartGalleryAdapter
import kotlinx.android.synthetic.main.fragment_fanart_gallery.*

@SuppressLint("SetTextI18n", "DefaultLocale", "SourceLockedOrientationActivity")
class FanartGalleryFragment : BaseFragment<FanartGalleryViewModel>(R.layout.fragment_fanart_gallery) {

  companion object {
    const val ARG_SHOW_ID = "ARG_SHOW_ID"
  }

  override val viewModel by viewModels<FanartGalleryViewModel> { viewModelFactory }

  private val showId by lazy { IdTrakt(arguments?.getLong(ARG_SHOW_ID, -1) ?: -1) }
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
      loadImage(showId)
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
      findNavController().popBackStack()
    }
  }
}
