package com.michaldrabik.ui_comments.post

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.MessageEvent.Type
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_comments.R
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
import kotlinx.android.synthetic.main.view_post_comment.*
import kotlinx.android.synthetic.main.view_post_comment.view.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PostCommentBottomSheet : BaseBottomSheetFragment<PostCommentViewModel>() {

  private val showTraktId by lazy { IdTrakt(requireArguments().getLong(ARG_SHOW_ID)) }
  private val movieTraktId by lazy { IdTrakt(requireArguments().getLong(ARG_MOVIE_ID)) }
  private val episodeTraktId by lazy { IdTrakt(requireArguments().getLong(ARG_EPISODE_ID)) }

  private val replyCommentId by lazy { IdTrakt(requireArguments().getLong(ARG_COMMENT_ID)) }
  private val replyUser by lazy { requireArguments().getString(ARG_REPLY_USER, "") }

  override val layoutResId = R.layout.view_post_comment

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    return inflater.cloneInContext(contextThemeWrapper).inflate(layoutResId, container, false)
  }

  override fun createViewModel() =
    ViewModelProvider(this).get(PostCommentViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView(view)

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          launch { messageState.collect { renderSnackbar(it) } }
        }
      }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun setupView(view: View) {
    view.run {
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
        viewPostCommentInput.isEnabled = !it
        viewPostCommentInputValue.isEnabled = !it
        viewPostCommentSpoilersCheck.isEnabled = !it
        viewPostCommentProgress.visibleIf(it)
        val commentText = viewPostCommentInputValue.text.toString()
        viewPostCommentButton.isEnabled = !it && isCommentValid(commentText)
        viewPostCommentButton.visibleIf(!it, gone = false)
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
          dismiss()
        }
      }
    }
  }

  private fun renderSnackbar(message: MessageEvent) {
    message.consume()?.let {
      when (message.type) {
        Type.INFO -> viewPostCommentSnackHost.showInfoSnackbar(getString(it))
        Type.ERROR -> viewPostCommentSnackHost.showErrorSnackbar(getString(it))
      }
    }
  }

  private fun isCommentValid(text: String) =
    text.trim().isNotEmpty() && text.trim().split(" ").count { it.length > 1 } >= 5
}
