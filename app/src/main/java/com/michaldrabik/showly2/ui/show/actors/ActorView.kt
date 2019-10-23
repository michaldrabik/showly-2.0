package com.michaldrabik.showly2.ui.show.actors

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.michaldrabik.showly2.Config.TVDB_IMAGE_BASE_URL
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Actor
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.visible
import com.michaldrabik.showly2.utilities.extensions.withFailListener
import kotlinx.android.synthetic.main.view_actor.view.*

class ActorView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.actorTileCorner) }

  init {
    inflate(context, R.layout.view_actor, this)
    layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
  }

  fun bind(item: Actor, clickListener: (Actor) -> Unit) {
    clear()
    setOnClickListener { clickListener(item) }
    actorName.text = item.name.split(" ").joinToString("\n")
    loadImage(item)
  }

  private fun loadImage(actor: Actor) {
    if (actor.image.isBlank()) {
      actorPlaceholder.visible()
      return
    }
    Glide.with(this)
      .load("$TVDB_IMAGE_BASE_URL${actor.image}")
      .transform(CenterCrop(), RoundedCorners(cornerRadius))
      .transition(withCrossFade(200))
      .withFailListener { actorPlaceholder.visible() }
      .into(actorImage)
  }

  private fun clear() {
    actorName.text = ""
    actorPlaceholder.gone()
    Glide.with(this).clear(actorImage)
  }
}