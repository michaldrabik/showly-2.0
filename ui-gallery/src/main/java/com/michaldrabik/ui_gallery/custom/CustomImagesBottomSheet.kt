package com.michaldrabik.ui_gallery.custom

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_gallery.R
import com.michaldrabik.ui_gallery.custom.di.UiCustomImagesComponentProvider
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.ImageFamily
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_FAMILY
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_FANART_URL
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_POSTER_URL
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_TYPE
import kotlinx.android.synthetic.main.view_custom_images.*
import kotlinx.android.synthetic.main.view_custom_images.view.*

class CustomImagesBottomSheet : BaseBottomSheetFragment<CustomImagesViewModel>() {

  private val family by lazy { arguments?.getSerializable(ARG_FAMILY) as ImageFamily }
  private val showTraktId by lazy { IdTrakt(requireArguments().getLong(ARG_SHOW_ID)) }
  private val movieTraktId by lazy { IdTrakt(requireArguments().getLong(ARG_MOVIE_ID)) }
  private val posterUrl by lazy { requireArguments().getString(ARG_POSTER_URL) }
  private val fanartUrl by lazy { requireArguments().getString(ARG_FANART_URL) }

  private val cornerRadius by lazy { requireContext().dimenToPx(R.dimen.customImagesCorner) }

  override val layoutResId = R.layout.view_custom_images

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiCustomImagesComponentProvider).provideCustomImagesComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    return inflater.cloneInContext(contextThemeWrapper).inflate(layoutResId, container, false)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(CustomImagesViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it) })
      loadPoster(showTraktId, movieTraktId, posterUrl, family)
      loadFanart(showTraktId, movieTraktId, fanartUrl, family)
    }
    setupView(view)
  }

  private fun setupView(view: View) {
    view.run {
      viewCustomImagesPosterLayout.onClick { showGallery(POSTER) }
      viewCustomImagesFanartLayout.onClick { showGallery(FANART) }
      viewCustomImagesApplyButton.onClick { dismiss() }
    }
  }

  private fun showGallery(type: ImageType) {
    val bundle = bundleOf(
      ARG_SHOW_ID to showTraktId.id,
      ARG_MOVIE_ID to movieTraktId.id,
      ARG_FAMILY to family,
      ARG_TYPE to type
    )
    navigateTo(R.id.actionCustomImagesDialogToArtGallery, bundle)
  }

  //
//  private fun getDateString(): String {
//    val millis = episode.firstAired?.toInstant()?.toEpochMilli() ?: -1
//    return if (millis == -1L) {
//      getString(R.string.textTba)
//    } else {
//      com.michaldrabik.common.extensions.dateFromMillis(millis)
//        .toLocalTimeZone()
//        .toDisplayString()
//    }
//  }

  //  private fun openRateDialog(rating: Int, showRemove: Boolean) {
//    val context = requireContext()
//    val rateView = RateView(context).apply {
//      setPadding(context.dimenToPx(R.dimen.spaceNormal))
//      setRating(rating)
//    }
//    MaterialAlertDialogBuilder(context, R.style.AlertDialog)
//      .setBackground(ContextCompat.getDrawable(context, R.drawable.bg_dialog))
//      .setView(rateView)
//      .setPositiveButton(R.string.textRate) { _, _ -> viewModel.addRating(rateView.getRating(), episode, showTraktId) }
//      .setNegativeButton(R.string.textCancel) { _, _ -> }
//      .apply {
//        if (showRemove) {
//          setNeutralButton(R.string.textRateDelete) { _, _ -> viewModel.deleteRating(episode) }
//        }
//      }
//      .show()
//  }
//
  @SuppressLint("SetTextI18n")
  private fun render(uiModel: CustomImagesUiModel) {

    fun loadImage(url: String, imageView: ImageView, progressView: View) {
      progressView.visible()
      Glide.with(requireContext())
        .load(url)
        .transform(CenterCrop(), RoundedCorners(cornerRadius))
        .withSuccessListener { progressView.gone() }
        .withFailListener { progressView.gone() }
        .into(imageView)
    }

    uiModel.run {
      posterImage?.let {
        viewCustomImagesPosterAddButton.gone()
        loadImage(
          it.fullFileUrl,
          viewCustomImagesPosterImage,
          viewCustomImagesPosterProgress
        )
      }
      fanartImage?.let {
        viewCustomImagesFanartAddButton.gone()
        loadImage(
          it.fullFileUrl,
          viewCustomImagesFanartImage,
          viewCustomImagesFanartProgress
        )
      }
    }
  }
//
//  private fun renderSnackbar(message: MessageEvent) {
//    message.consume()?.let {
//      when (message.type) {
//        INFO -> episodeDetailsSnackbarHost.showInfoSnackbar(getString(it))
//        ERROR -> episodeDetailsSnackbarHost.showErrorSnackbar(getString(it))
//      }
//    }
//  }
//
//  override fun onDismiss(dialog: DialogInterface) {
//    onEpisodeWatchedClick = null
//    super.onDismiss(dialog)
//  }
}
