package com.michaldrabik.ui_comments.post.di

import com.michaldrabik.ui_comments.post.PostCommentBottomSheet
import dagger.Subcomponent

@Subcomponent
interface UiPostCommentComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(): UiPostCommentComponent
  }

  fun inject(fragment: PostCommentBottomSheet)
}
