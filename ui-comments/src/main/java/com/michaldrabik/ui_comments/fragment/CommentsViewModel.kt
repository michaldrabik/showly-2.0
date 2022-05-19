package com.michaldrabik.ui_comments.fragment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_comments.fragment.CommentsFragment.Companion.Options
import com.michaldrabik.ui_comments.fragment.cases.LoadCommentsCase
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_OPTIONS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val commentsCase: LoadCommentsCase,
) : ViewModel() {

  private val commentsState = MutableStateFlow<List<Comment>?>(null)
  private val loadingState = MutableStateFlow(false)

  init {
    val options: Options? = savedStateHandle[ARG_OPTIONS]
    options?.let { loadComments(it.id, it.mode) }
  }

  fun loadComments(id: IdTrakt, mode: Mode) {
    commentsState.value = null
    viewModelScope.launch {
      try {
        val comments = commentsCase.loadComments(id, mode)
        commentsState.value = comments
      } catch (error: Throwable) {
        commentsState.value = emptyList()
        Timber.e(error)
      }
    }
  }

  val uiState = combine(
    commentsState,
    loadingState
  ) { s1, s2 ->
    CommentsUiState(
      comments = s1,
      isLoading = s2
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = CommentsUiState()
  )
}
