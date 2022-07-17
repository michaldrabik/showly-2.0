package com.michaldrabik.ui_comments.post

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.requireLong
import com.michaldrabik.ui_base.utilities.extensions.requireString
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_comments.R
import com.michaldrabik.ui_comments.databinding.ViewPostCommentBinding
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_COMMENT
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_COMMENT_ACTION
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_COMMENT_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_EPISODE_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_REPLY_USER
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_COMMENT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostCommentBottomSheet : BaseBottomSheetFragment(R.layout.view_post_comment) {

  private val showTraktId by lazy { IdTrakt(requireLong(ARG_SHOW_ID)) }
  private val movieTraktId by lazy { IdTrakt(requireLong(ARG_MOVIE_ID)) }
  private val episodeTraktId by lazy { IdTrakt(requireLong(ARG_EPISODE_ID)) }
  private val replyCommentId by lazy { IdTrakt(requireLong(ARG_COMMENT_ID)) }
  private val replyUser by lazy { requireString(ARG_REPLY_USER, default = "") }

  private val binding by viewBinding(ViewPostCommentBinding::bind)
  private val viewModel by viewModels<PostCommentViewModel>()

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.messageFlow.collect { renderSnackbar(it) } },
    )
  }

  @SuppressLint("SetTextI18n")
  private fun setupView() {
    with(binding) {
      viewPostCommentInputValue.doOnTextChanged { text, _, _, _ ->
        val isValid =
          !text?.trim().isNullOrEmpty() &&
            (text?.trim()?.split(" ")?.count { it.length > 1 } ?: 0) >= 5
        viewPostCommentButton.isEnabled = isValid
      }
      viewPostCommentButton.onClick {
        val commentText = viewPostCommentInputValue.text.toString()
        val isSpoiler = viewPostCommentSpoilersCheck.isChecked
        when {
          replyCommentId.id > 0 -> viewModel.postReply(replyCommentId, commentText, isSpoiler)
          showTraktId.id > 0 -> viewModel.postShowComment(showTraktId, commentText, isSpoiler)
          movieTraktId.id > 0 -> viewModel.postMovieComment(movieTraktId, commentText, isSpoiler)
          episodeTraktId.id > 0 -> viewModel.postEpisodeComment(episodeTraktId, commentText, isSpoiler)
          else -> error("Invalid comment target.")
        }
      }
      if (replyUser.isNotEmpty() && replyCommentId.id != 0L) {
        viewPostCommentInputValue.setText("@$replyUser ")
      }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: PostCommentUiState) {
    uiState.run {
      isLoading.let {
        with(binding) {
          viewPostCommentInput.isEnabled = !it
          viewPostCommentInputValue.isEnabled = !it
          viewPostCommentSpoilersCheck.isEnabled = !it
          viewPostCommentProgress.visibleIf(it)
          val commentText = viewPostCommentInputValue.text.toString()
          viewPostCommentButton.isEnabled = !it && isCommentValid(commentText)
          viewPostCommentButton.visibleIf(!it, gone = false)
        }
      }
      isSuccess?.let {
        it.consume()?.let { commentBundle ->
          setFragmentResult(
            REQUEST_COMMENT,
            bundleOf(
              ARG_COMMENT_ACTION to commentBundle.first,
              ARG_COMMENT to commentBundle.second
            )
          )
          closeSheet()
        }
      }
    }
  }

  private fun renderSnackbar(message: MessageEvent) {
    when (message) {
      is MessageEvent.Info -> binding.viewPostCommentSnackHost.showInfoSnackbar(getString(message.textRestId))
      is MessageEvent.Error -> binding.viewPostCommentSnackHost.showErrorSnackbar(getString(message.textRestId))
    }
  }

  private fun isCommentValid(text: String) =
    text.trim().isNotEmpty() && text.trim().split(" ").count { it.length > 1 } >= 5
}
