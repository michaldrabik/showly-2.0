package com.michaldrabik.ui_people.details.recycler.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_people.R
import com.michaldrabik.ui_people.details.recycler.PersonDetailsItem
import kotlinx.android.synthetic.main.view_person_details_info.view.*

class PersonDetailsInfoView : ConstraintLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val topLeftCornerRadius by lazy { context.dimenToPx(R.dimen.personImageCorner).toFloat() }
  private val cornerRadius by lazy { context.dimenToPx(R.dimen.mediaTileCorner).toFloat() }
  private val spaceNormal by lazy { context.dimenToPx(R.dimen.spaceNormal) }

  var onLinksClickListener: ((Person) -> Unit)? = null
  var onImageClickListener: (() -> Unit)? = null

  init {
    inflate(context, R.layout.view_person_details_info, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    updatePadding(left = spaceNormal, right = spaceNormal)
    clipToPadding = false
  }

  fun bind(item: PersonDetailsItem.MainInfo) {
    viewPersonDetailsTitle.text = item.person.name
    viewPersonDetailsSubtitle.text = item.person.characters.joinToString(", ")
    viewPersonDetailsLinkIcon.onClick { onLinksClickListener?.invoke(item.person) }
    viewPersonDetailsImage.onClick { onImageClickListener?.invoke() }
    viewPersonDetailsPlaceholder.onClick { onImageClickListener?.invoke() }

    item.person.birthday?.let { date ->
      viewPersonDetailsBirthdayLabel.visible()
      viewPersonDetailsBirthdayValue.visible()
      viewPersonDetailsAgeLabel.visible()
      viewPersonDetailsAgeValue.visible()
      val birthdayText = item.dateFormat?.format(date)
        ?.capitalizeWords()
        ?.plus(if (!item.person.birthplace.isNullOrBlank()) "\n${item.person.birthplace}" else "")
      viewPersonDetailsBirthdayValue.text = birthdayText
      viewPersonDetailsAgeValue.text = item.person.getAge().toString()
    }
    item.person.deathday?.let { date ->
      viewPersonDetailsDeathdayLabel.visible()
      viewPersonDetailsDeathdayValue.visible()
      viewPersonDetailsDeathdayValue.text = item.dateFormat?.format(date)?.capitalizeWords()
    }
    viewPersonDetailsProgress.visibleIf(item.isLoading)

    renderImage(item.person)
  }

  private fun renderImage(person: Person) {
    Glide.with(this).clear(viewPersonDetailsImage)

    if (person.imagePath.isNullOrBlank()) {
      viewPersonDetailsImage.gone()
      viewPersonDetailsPlaceholder.visible()
      return
    }

    viewPersonDetailsImage.visible()
    viewPersonDetailsPlaceholder.gone()

    Glide.with(this)
      .load("${Config.TMDB_IMAGE_BASE_ACTOR_URL}${person.imagePath}")
      .transform(CenterCrop(), GranularRoundedCorners(topLeftCornerRadius, cornerRadius, cornerRadius, cornerRadius))
      .transition(DrawableTransitionOptions.withCrossFade(Config.IMAGE_FADE_DURATION_MS))
      .withFailListener {
        viewPersonDetailsImage.gone()
        viewPersonDetailsPlaceholder.visible()
      }
      .into(viewPersonDetailsImage)
  }
}
