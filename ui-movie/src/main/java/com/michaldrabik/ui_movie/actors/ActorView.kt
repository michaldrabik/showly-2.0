package com.michaldrabik.ui_movie.actors

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy.DATA
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.michaldrabik.common.Config.IMAGE_FADE_DURATION_MS
import com.michaldrabik.common.Config.TMDB_IMAGE_BASE_ACTOR_URL
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_model.Actor
import com.michaldrabik.ui_movie.R
import kotlinx.android.synthetic.main.view_actor_movie.view.*

class ActorView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.actorMovieTileCorner) }

  init {
    inflate(context, R.layout.view_actor_movie, this)
    layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    clipChildren = false
  }

  fun bind(item: Actor, clickListener: (Actor) -> Unit) {
    clear()
    tag = item.tmdbId
    setOnClickListener {
      if (item.image.isNotBlank()) clickListener(item)
    }
    actorMovieName.text = item.name.split(" ").joinToString("\n")
    loadImage(item)
  }

  private fun loadImage(actor: Actor) {
    if (actor.image.isBlank()) {
      actorMoviePlaceholder.visible()
      actorMovieImage.gone()
      return
    }

    Glide.with(this)
      .load("$TMDB_IMAGE_BASE_ACTOR_URL${actor.image}")
      .diskCacheStrategy(DATA)
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .transition(withCrossFade(IMAGE_FADE_DURATION_MS))
      .withFailListener {
        actorMoviePlaceholder.visible()
        actorMovieImage.gone()
      }
      .into(actorMovieImage)
  }

  private fun clear() {
    actorMovieImage.visible()
    actorMoviePlaceholder.gone()
    Glide.with(this).clear(actorMovieImage)
  }
}
