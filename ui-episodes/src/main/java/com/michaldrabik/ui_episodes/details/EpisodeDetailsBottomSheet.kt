package com.michaldrabik.ui_episodes.details

import android.annotation.SuppressLint
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.NORMAL
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.michaldrabik.common.Config
import com.michaldrabik.common.Config.IMAGE_FADE_DURATION_MS
import com.michaldrabik.common.extensions.dateFromMillis
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet.Options.Operation
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet.Options.Type
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.requireParcelable
import com.michaldrabik.ui_base.utilities.extensions.setTextFade
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_comments.CommentView
import com.michaldrabik.ui_episodes.R
import com.michaldrabik.ui_episodes.databinding.ViewEpisodeDetailsBinding
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_EPISODE_TAB_SELECTED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_EPISODE_WATCHED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_NEW_COMMENT
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_COMMENT
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_COMMENT_ACTION
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_COMMENT_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_EPISODE_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_OPTIONS
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_REPLY_USER
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_COMMENT
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_EPISODE_DETAILS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.util.Locale.ENGLISH

@AndroidEntryPoint
class EpisodeDetailsBottomSheet : BaseBottomSheetFragment(R.layout.view_episode_details) {

  companion object {
    fun createBundle(
      ids: Ids,
      episode: Episode,
      seasonEpisodesIds: List<Int>?,
      isWatched: Boolean,
      showButton: Boolean,
      showTabs: Boolean
    ): Bundle {
      val options = Options(
        ids = ids,
        episode = episode,
        seasonEpisodesIds = seasonEpisodesIds,
        isWatched = isWatched,
        showButton = showButton,
        showTabs = showTabs
      )
      return bundleOf(ARG_OPTIONS to options)
    }
  }

  private val viewModel by viewModels<EpisodeDetailsViewModel>()
  private val binding by viewBinding(ViewEpisodeDetailsBinding::bind)

  private val options by lazy { requireParcelable<Options>(ARG_OPTIONS) }
  private val cornerRadius by lazy { dimenToPx(R.dimen.bottomSheetCorner).toFloat() }

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

    with(viewModel) {
      launchAndRepeatStarted(
        { uiState.collect { render(it) } },
        { messageFlow.collect { renderSnackbar(it) } },
        doAfterLaunch = {
          val (ids, episode, seasonEpisodes) = options
          loadSeason(ids.trakt, episode, seasonEpisodes?.toIntArray())
          loadTranslation(ids.trakt, episode)
          loadImage(ids.tmdb, episode)
          loadRatings(episode)
        }
      )
    }
  }

  private fun setupView() {
    binding.run {
      val (ids, episode, _, isWatched, showButton, showTabs) = options
      episodeDetailsTitle.text = when (episode.title) {
        "Episode ${episode.number}" -> String.format(ENGLISH, requireContext().getString(R.string.textEpisode), episode.number)
        else -> episode.title
      }
      episodeDetailsOverview.text =
        if (episode.overview.isBlank()) getString(R.string.textNoDescription) else episode.overview
      episodeDetailsButton.run {
        visibleIf(showButton && !isWatched)
        onClick {
          setFragmentResult(REQUEST_EPISODE_DETAILS, bundleOf(ACTION_EPISODE_WATCHED to !isWatched))
          closeSheet()
        }
      }
      episodeDetailsRatingLayout.visibleIf(episode.votes > 0)
      if (!showTabs) episodeDetailsTabs.gone()
      episodeDetailsRating.text = String.format(ENGLISH, getString(R.string.textVotes), episode.rating, episode.votes)
      episodeDetailsCommentsButton.text = String.format(ENGLISH, getString(R.string.textLoadCommentsCount), episode.commentCount)
      episodeDetailsCommentsButton.onClick {
        viewModel.loadComments(ids.trakt, episode.season, episode.number)
      }
      episodeDetailsPostCommentButton.onClick { openPostCommentSheet() }
    }
  }

  private fun openRateDialog() {
    setFragmentResultListener(NavigationArgs.REQUEST_RATING) { _, bundle ->
      when (bundle.getParcelable<Operation>(NavigationArgs.RESULT)) {
        Operation.SAVE -> renderSnackbar(MessageEvent.Info(R.string.textRateSaved))
        Operation.REMOVE -> renderSnackbar(MessageEvent.Info(R.string.textRateRemoved))
        else -> Timber.w("Unknown result.")
      }
      viewModel.loadRatings(options.episode)
      setFragmentResult(REQUEST_EPISODE_DETAILS, bundleOf(NavigationArgs.ACTION_RATING_CHANGED to true))
    }
    val bundle = RatingsBottomSheet.createBundle(options.episode.ids.trakt, Type.EPISODE)
    navigateTo(R.id.actionEpisodeDetailsDialogToRate, bundle)
  }

  private fun openPostCommentSheet(comment: Comment? = null) {
    setFragmentResultListener(REQUEST_COMMENT) { _, bundle ->
      renderSnackbar(MessageEvent.Info(R.string.textCommentPosted))
      when (bundle.getString(ARG_COMMENT_ACTION)) {
        ACTION_NEW_COMMENT -> {
          val newComment = bundle.getParcelable<Comment>(ARG_COMMENT)!!
          viewModel.addNewComment(newComment)
        }
      }
    }
    val bundle = when {
      comment != null -> bundleOf(
        ARG_COMMENT_ID to comment.getReplyId(),
        ARG_REPLY_USER to comment.user.username
      )
      else -> bundleOf(ARG_EPISODE_ID to options.episode.ids.trakt.id)
    }
    navigateTo(R.id.actionEpisodeDetailsDialogToPostComment, bundle)
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: EpisodeDetailsUiState) {
    uiState.run {
      with(binding) {
        val episode = options.episode
        dateFormat?.let {
          val millis = episode.firstAired?.toInstant()?.toEpochMilli() ?: -1
          val date = if (millis == -1L) {
            getString(R.string.textTba)
          } else {
            it.format(dateFromMillis(millis).toLocalZone()).capitalizeWords()
          }
          val name = String.format(ENGLISH, getString(R.string.textSeasonEpisodeDate), episode.season, episode.number, date)
          val runtime = "${episode.runtime} ${getString(R.string.textMinutesShort)}"
          episodeDetailsName.text = if (episode.runtime > 0) "$name | $runtime" else name
        }
        isImageLoading.let { episodeDetailsProgress.visibleIf(it) }
        image?.let { renderImage(it) }
        isCommentsLoading.let {
          episodeDetailsButtons.visibleIf(!it)
          episodeDetailsCommentsProgress.visibleIf(it)
        }
        episodes?.let { renderEpisodes(it) }
        comments?.let { comments ->
          episodeDetailsComments.removeAllViews()
          comments.forEach {
            val view = CommentView(requireContext()).apply {
              bind(it, commentsDateFormat)
              if (it.replies > 0) {
                onRepliesClickListener = { comment -> viewModel.loadCommentReplies(comment) }
              }
              if (it.isSignedIn) {
                onReplyClickListener = { comment -> openPostCommentSheet(comment) }
              }
              if (it.replies == 0L && it.isMe && it.isSignedIn) {
                onDeleteClickListener = { comment -> openDeleteCommentDialog(comment) }
              }
            }
            episodeDetailsComments.addView(view)
          }
          episodeDetailsCommentsLabel.fadeIf(comments.isNotEmpty())
          episodeDetailsComments.fadeIf(comments.isNotEmpty())
          episodeDetailsCommentsEmpty.fadeIf(comments.isEmpty())
          episodeDetailsPostCommentButton.fadeIf(isSignedIn)
          episodeDetailsCommentsButton.isEnabled = false
          episodeDetailsCommentsButton.text = String.format(ENGLISH, getString(R.string.textLoadCommentsCount), comments.size)
        }
        ratingState?.let { state ->
          episodeDetailsRateProgress.visibleIf(state.rateLoading == true)
          episodeDetailsRateButton.visibleIf(state.rateLoading == false)
          episodeDetailsRateButton.onClick {
            if (state.rateAllowed == true) {
              openRateDialog()
            } else {
              renderSnackbar(MessageEvent.Info(R.string.textSignBeforeRate))
            }
          }
          if (state.hasRating()) {
            episodeDetailsRateButton.setTypeface(null, BOLD)
            episodeDetailsRateButton.text = "${state.userRating?.rating}/10"
          } else {
            episodeDetailsRateButton.setTypeface(null, NORMAL)
            episodeDetailsRateButton.setText(R.string.textRate)
          }
        }
        translation?.let { t ->
          t.consume()?.let {
            if (it.title.isNotBlank()) {
              episodeDetailsTitle.setTextFade(it.title, duration = 0)
            }
            if (it.overview.isNotBlank()) {
              episodeDetailsOverview.setTextFade(it.overview, duration = 0)
            }
          }
        }
      }
    }
  }

  private fun renderImage(image: Image) =
    Glide.with(this@EpisodeDetailsBottomSheet)
      .load("${Config.TMDB_IMAGE_BASE_STILL_URL}${image.fileUrl}")
      .transform(CenterCrop(), GranularRoundedCorners(cornerRadius, cornerRadius, 0F, 0F))
      .transition(DrawableTransitionOptions.withCrossFade(IMAGE_FADE_DURATION_MS))
      .withFailListener { binding.episodeDetailsImagePlaceholder.visible() }
      .into(binding.episodeDetailsImage)

  private fun renderEpisodes(episodes: List<Episode>) {
    with(binding.episodeDetailsTabs) {
      removeAllTabs()
      removeOnTabSelectedListener(tabSelectedListener)
      episodes.forEach {
        addTab(
          newTab()
            .setText("${options.episode.season}x${it.number.toString().padStart(2, '0')}")
            .setTag(it)
        )
      }
      val index = episodes.indexOfFirst { it.number == options.episode.number }
      // Small trick to avoid UI tab change flick
      getTabAt(index)?.select()
      post {
        getTabAt(index)?.select()
        addOnTabSelectedListener(tabSelectedListener)
      }
      if (options.showTabs && episodes.isNotEmpty()) {
        fadeIn(duration = 200, startDelay = 100, withHardware = true)
      } else {
        gone()
      }
    }
  }

  private fun renderSnackbar(message: MessageEvent) {
    when (message) {
      is MessageEvent.Info -> binding.episodeDetailsSnackbarHost.showInfoSnackbar(getString(message.textRestId))
      is MessageEvent.Error -> binding.episodeDetailsSnackbarHost.showErrorSnackbar(getString(message.textRestId))
    }
  }

  private fun openDeleteCommentDialog(comment: Comment) {
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setTitle(R.string.textCommentConfirmDeleteTitle)
      .setMessage(R.string.textCommentConfirmDelete)
      .setPositiveButton(R.string.textYes) { _, _ -> viewModel.deleteComment(comment) }
      .setNegativeButton(R.string.textNo) { _, _ -> }
      .show()
  }

  private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
    override fun onTabSelected(tab: TabLayout.Tab?) {
      binding.episodeDetailsTabs.removeOnTabSelectedListener(this)
      closeSheet()
      setFragmentResult(REQUEST_EPISODE_DETAILS, bundleOf(ACTION_EPISODE_TAB_SELECTED to tab?.tag))
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
    override fun onTabReselected(tab: TabLayout.Tab?) = Unit
  }

  @Parcelize
  private data class Options(
    val ids: Ids,
    val episode: Episode,
    val seasonEpisodesIds: List<Int>?,
    val isWatched: Boolean,
    val showButton: Boolean,
    val showTabs: Boolean
  ) : Parcelable
}
