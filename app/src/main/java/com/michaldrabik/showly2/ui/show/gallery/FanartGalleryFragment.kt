package com.michaldrabik.showly2.ui.show.gallery

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.ui.common.base.BaseFragment

@SuppressLint("SetTextI18n", "DefaultLocale")
class FanartGalleryFragment : BaseFragment<FanartGalleryViewModel>() {

  override val layoutResId = R.layout.fragment_fanart_gallery

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun createViewModel(provider: ViewModelProvider) =
    provider.get(FanartGalleryViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    requireActivity().requestedOrientation = SCREEN_ORIENTATION_LANDSCAPE
    viewModel.run {
      //      uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
//      messageStream.observe(viewLifecycleOwner, Observer { showInfoSnackbar(it!!) })
//      errorStream.observe(viewLifecycleOwner, Observer { showErrorSnackbar(it!!) })
//      loadShowDetails(showId, requireContext().applicationContext)
    }
  }

  override fun onResume() {
    super.onResume()
    handleBackPressed()
  }

  override fun onDestroyView() {
//    requireActivity().requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
    super.onDestroyView()
  }

  private fun handleBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      remove()
      findNavController().popBackStack()
    }
  }
}
