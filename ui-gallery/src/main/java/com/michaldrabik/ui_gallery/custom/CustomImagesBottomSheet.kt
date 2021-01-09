package com.michaldrabik.ui_gallery.custom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_gallery.R
import com.michaldrabik.ui_gallery.custom.di.UiCustomImagesComponentProvider
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.ImageFamily
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_TYPE
import kotlinx.android.synthetic.main.view_custom_images.view.*

class CustomImagesBottomSheet : BaseBottomSheetFragment<CustomImagesViewModel>() {

  private val showTraktId by lazy { IdTrakt(requireArguments().getLong(ARG_SHOW_ID)) }
  private val movieTraktId by lazy { IdTrakt(requireArguments().getLong(ARG_MOVIE_ID)) }
  private val type by lazy { arguments?.getSerializable(ARG_TYPE) as ImageFamily }

  private val cornerRadius by lazy { requireContext().dimenToPx(R.dimen.customImagesCorner).toFloat() }

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
//    viewModel.run {
//      uiLiveData.observe(viewLifecycleOwner, { render(it) })
//      messageLiveData.observe(viewLifecycleOwner, { renderSnackbar(it) })
//      loadTranslation(showTraktId, episode)
//      loadImage(showTmdbId, episode)
//      loadRatings(episode)
//    }
    setupView(view)
  }

  private fun setupView(view: View) {
    view.run {
      viewCustomImagesPosterLayout.onClick { showGallery() }
      viewCustomImagesFanartLayout.onClick { showGallery() }
      viewCustomImagesApplyButton.onClick { dismiss() }
    }
  }

  private fun showGallery() {
    val bundle = bundleOf(
      ARG_SHOW_ID to showTraktId.id,
      ARG_MOVIE_ID to movieTraktId.id,
      ARG_TYPE to type
    )
    navigateTo(R.id.actionCustomImagesDialogToFanartGallery, bundle)
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
//
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
//  @SuppressLint("SetTextI18n")
//  private fun render(uiModel: EpisodeDetailsUiModel) {
//    uiModel.run {
//      imageLoading?.let { episodeDetailsProgress.visibleIf(it) }
//      image?.let {
//        Glide.with(this@CustomImagesBottomSheet)
//          .load(it.fullFileUrlEpisode)
//          .transform(CenterCrop(), GranularRoundedCorners(cornerRadius, cornerRadius, 0F, 0F))
//          .transition(DrawableTransitionOptions.withCrossFade(IMAGE_FADE_DURATION_MS))
//          .withFailListener { episodeDetailsImagePlaceholder.visible() }
//          .into(episodeDetailsImage)
//      }
//      commentsLoading?.let {
//        episodeDetailsButtons.visibleIf(!it && comments?.isEmpty() == true)
//        episodeDetailsCommentsProgress.visibleIf(it)
//      }
//      comments?.let { comments ->
//        episodeDetailsComments.removeAllViews()
//        comments.forEach {
//          val view = CommentView(requireContext()).apply {
//            bind(it)
//          }
//          episodeDetailsComments.addView(view)
//        }
//        episodeDetailsCommentsLabel.fadeIf(comments.isNotEmpty())
//        episodeDetailsComments.fadeIf(comments.isNotEmpty())
//        episodeDetailsCommentsEmpty.fadeIf(comments.isEmpty())
//      }
//      ratingState?.let { state ->
//        episodeDetailsRateProgress.visibleIf(state.rateLoading == true)
//        episodeDetailsRateButton.visibleIf(state.rateLoading == false)
//        episodeDetailsRateButton.onClick {
//          if (state.rateAllowed == true) {
//            val rate = state.userRating?.rating ?: INITIAL_RATING
//            openRateDialog(rate, rate != 0)
//          } else {
//            renderSnackbar(info(R.string.textSignBeforeRate))
//          }
//        }
//        if (state.hasRating()) {
//          episodeDetailsRateButton.text = "${state.userRating?.rating}/10"
//          episodeDetailsRateButton.setTextColor(requireContext().colorFromAttr(android.R.attr.colorAccent))
//        } else {
//          episodeDetailsRateButton.setText(R.string.textRate)
//          episodeDetailsRateButton.setTextColor(requireContext().colorFromAttr(android.R.attr.textColorPrimary))
//        }
//      }
//      translation?.let { t ->
//        t.consume()?.let {
//          if (it.overview.isNotBlank()) {
//            episodeDetailsOverview.setTextFade(it.overview, 0)
//            if (it.title.isNotBlank()) {
//              episodeDetailsTitle.setTextFade(it.title, 0)
//            }
//          }
//        }
//      }
//    }
//  }
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
