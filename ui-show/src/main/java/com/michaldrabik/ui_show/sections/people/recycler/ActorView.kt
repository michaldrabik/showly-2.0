package com.michaldrabik.ui_show.sections.people.recycler

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
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
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_show.R
import com.michaldrabik.ui_show.databinding.ViewActorBinding

class ActorView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewActorBinding.inflate(LayoutInflater.from(context), this)
  private val cornerRadius by lazy { context.dimenToPx(R.dimen.actorTileCorner) }

  init {
    layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    clipChildren = false
  }

  fun bind(item: Person, clickListener: (Person) -> Unit) {
    clear()
    tag = item.ids.tmdb.id
    onClick { clickListener(item) }
    binding.actorName.text = item.name.split(" ").joinToString("\n")
    loadImage(item)
  }

  private fun loadImage(person: Person) {
    with(binding) {
      if (person.imagePath.isNullOrBlank()) {
        actorPlaceholder.visible()
        actorImage.gone()
        return
      }

      Glide.with(this@ActorView)
        .load("$TMDB_IMAGE_BASE_ACTOR_URL${person.imagePath}")
        .diskCacheStrategy(DATA)
        .transform(CenterCrop(), RoundedCorners(cornerRadius))
        .transition(withCrossFade(IMAGE_FADE_DURATION_MS))
        .withFailListener {
          actorPlaceholder.visible()
          actorImage.gone()
        }
        .into(actorImage)
    }
  }

  private fun clear() {
    with(binding) {
      actorPlaceholder.gone()
      actorImage.visible()
      Glide.with(this@ActorView).clear(actorImage)
    }
  }
}
