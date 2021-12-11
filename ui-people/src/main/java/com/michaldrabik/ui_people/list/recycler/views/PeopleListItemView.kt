package com.michaldrabik.ui_people.list.recycler.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.Person.Department
import com.michaldrabik.ui_model.Person.Job
import com.michaldrabik.ui_people.R
import com.michaldrabik.ui_people.list.recycler.PeopleListItem
import kotlinx.android.synthetic.main.view_people_list_item.view.*

class PeopleListItemView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  var onItemClickListener: ((Person) -> Unit)? = null

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.mediaTileCorner) }
  private val centerCropTransformation by lazy { CenterCrop() }
  private val cornersTransformation by lazy { RoundedCorners(cornerRadius) }

  private lateinit var item: PeopleListItem.PersonItem

  init {
    inflate(context, R.layout.view_people_list_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    viewPersonItemRoot.onClick { onItemClickListener?.invoke(item.person) }
  }

  @SuppressLint("SetTextI18n")
  fun bind(item: PeopleListItem.PersonItem) {
    clear()
    this.item = item

    viewPersonItemTitle.text = item.person.name
    val mainJob = item.person.jobs.firstOrNull { it != Job.UNKNOWN }
    viewPersonItemHeader.text = when (mainJob) {
      Job.DIRECTOR -> context.getString(R.string.textDirector)
      Job.WRITER, Job.STORY -> context.getString(R.string.textWriting)
      Job.SCREENPLAY -> context.getString(R.string.textScreenplay)
      Job.MUSIC, Job.ORIGINAL_MUSIC -> context.getString(R.string.textMusic)
      else -> when (item.person.department) {
        Department.ACTING -> context.getString(R.string.textActing)
        Department.DIRECTING -> context.getString(R.string.textDirector)
        Department.WRITING -> context.getString(R.string.textWriting)
        Department.SOUND -> context.getString(R.string.textMusic)
        Department.UNKNOWN -> "-"
      }
    }
    viewPersonItemDescription.visibleIf(item.person.episodesCount > 0)
    viewPersonItemDescription.text = "${context.getString(R.string.textEpisodes)}: ${item.person.episodesCount}"

    loadImage(item.person.imagePath)
  }

  private fun loadImage(imagePath: String?) {
    if (imagePath.isNullOrBlank()) {
      viewPersonItemImage.gone()
      viewPersonItemPlaceholder.visible()
      return
    }
    Glide.with(this)
      .load("${Config.TMDB_IMAGE_BASE_PROFILE_THUMB_URL}$imagePath")
      .transform(centerCropTransformation, cornersTransformation)
      .transition(DrawableTransitionOptions.withCrossFade(Config.IMAGE_FADE_DURATION_MS))
      .withSuccessListener {
        viewPersonItemPlaceholder.gone()
      }
      .withFailListener {
        viewPersonItemImage.gone()
        viewPersonItemPlaceholder.fadeIn(Config.IMAGE_FADE_DURATION_MS.toLong())
      }
      .into(viewPersonItemImage)
  }

  private fun clear() {
    viewPersonItemImage.visible()
    viewPersonItemPlaceholder.gone()
    Glide.with(this).clear(viewPersonItemImage)
  }
}
