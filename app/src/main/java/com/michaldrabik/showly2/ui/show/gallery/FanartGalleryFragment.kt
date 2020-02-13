package com.michaldrabik.showly2.ui.show.gallery

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.show.gallery.recycler.FanartGalleryAdapter
import com.michaldrabik.showly2.utilities.extensions.nextPage
import com.michaldrabik.showly2.utilities.extensions.onClick
import kotlinx.android.synthetic.main.fragment_fanart_gallery.*

@SuppressLint("SetTextI18n", "DefaultLocale")
class FanartGalleryFragment : BaseFragment<FanartGalleryViewModel>() {

  companion object {
    const val ARG_SHOW_ID = "ARG_SHOW_ID"
  }

  override val layoutResId = R.layout.fragment_fanart_gallery
  override val viewModel by viewModels<FanartGalleryViewModel> { viewModelFactory }

  private val showId by lazy { IdTrakt(arguments?.getLong(ARG_SHOW_ID, -1) ?: -1) }
  private val galleryAdapter by lazy { FanartGalleryAdapter() }

  override fun onCreate(savedInstanceState: Bundle?) {
    fragmentComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    requireActivity().requestedOrientation = SCREEN_ORIENTATION_LANDSCAPE
    setupView()
    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, Observer { render(it!!) })
      loadImage(showId)
    }
  }

  override fun onResume() {
    super.onResume()
    handleBackPressed()
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
