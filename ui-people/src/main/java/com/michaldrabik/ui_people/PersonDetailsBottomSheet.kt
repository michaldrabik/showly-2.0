package com.michaldrabik.ui_people

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_PERSON
import com.michaldrabik.ui_people.links.PersonLinksBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.view_person_details.*
import kotlinx.android.synthetic.main.view_person_details.view.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class PersonDetailsBottomSheet : BaseBottomSheetFragment<PersonDetailsViewModel>() {

  companion object {
    fun createBundle(person: Person) = bundleOf(ARG_PERSON to person)
  }

  override val layoutResId = R.layout.view_person_details

  private val person by lazy { requireArguments().getParcelable<Person>(ARG_PERSON) as Person }

  private val topLeftCornerRadius by lazy { dimenToPx(R.dimen.personImageCorner).toFloat() }
  private val cornerRadius by lazy { dimenToPx(R.dimen.mediaTileCorner).toFloat() }

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    return inflater.cloneInContext(contextThemeWrapper).inflate(layoutResId, container, false)
  }

  override fun createViewModel() = ViewModelProvider(this)[PersonDetailsViewModel::class.java]

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView(view)

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.loadDetails(person) }
    )
  }

  private fun setupView(view: View) {
    view.run {
      viewPersonDetailsBio.setInitialLines(5)
    }
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: PersonDetailsUiState) {
    uiState.run {
      personDetails?.let { it ->
        viewPersonDetailsTitle.text = it.name
        viewPersonDetailsSubtitle.text = it.character
        viewPersonDetailsBio.visibleIf(!it.bio.isNullOrBlank())
        viewPersonDetailsBio.text = it.bio

        it.birthday?.let { date ->
          viewPersonDetailsBirthdayLabel.visible()
          viewPersonDetailsBirthdayValue.visible()
          val birthdayText = dateFormat?.format(date)
            ?.capitalizeWords()
            ?.plus(it.getAge()?.let { age -> " ($age)" } ?: "")
            ?.plus(if (!it.birthplace.isNullOrBlank()) "\n${it.birthplace}" else "")
          viewPersonDetailsBirthdayValue.text = birthdayText
        }
        it.deathday?.let { date ->
          viewPersonDetailsDeathdayLabel.visible()
          viewPersonDetailsDeathdayValue.visible()
          viewPersonDetailsDeathdayValue.text = dateFormat?.format(date)?.capitalizeWords()
        }

        renderImage(it)

        viewPersonDetailsLinkIcon.onClick { _ ->
          val options = PersonLinksBottomSheet.createBundle(it)
          navigateTo(R.id.actionPersonDetailsDialogToLinks, options)
        }
      }
      isLoading?.let {
        viewPersonDetailsProgress.visibleIf(it)
        viewPersonDetailsLinkIcon.isClickable = !it
      }
    }
  }

  private fun renderImage(person: Person) {
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
