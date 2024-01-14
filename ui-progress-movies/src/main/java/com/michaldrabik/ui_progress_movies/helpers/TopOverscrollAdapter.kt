package com.michaldrabik.ui_progress_movies.helpers

import androidx.recyclerview.widget.RecyclerView
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter

class TopOverscrollAdapter(recycler: RecyclerView) : RecyclerViewOverScrollDecorAdapter(recycler) {
  override fun isInAbsoluteEnd() = false
}
