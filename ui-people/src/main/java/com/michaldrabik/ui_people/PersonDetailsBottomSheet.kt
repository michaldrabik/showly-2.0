package com.michaldrabik.ui_people

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_PERSON
import com.michaldrabik.ui_people.recycler.PersonDetailsAdapter
import com.michaldrabik.ui_people.recycler.PersonDetailsItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.view_person_details.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class PersonDetailsBottomSheet : BaseBottomSheetFragment<PersonDetailsViewModel>() {

  companion object {
    fun createBundle(person: Person) = bundleOf(ARG_PERSON to person)
  }

  override val layoutResId = R.layout.view_person_details

  private val person by lazy { requireArguments().getParcelable<Person>(ARG_PERSON) as Person }

  private var adapter: PersonDetailsAdapter? = null
  private var layoutManager: LinearLayoutManager? = null

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    return inflater.cloneInContext(contextThemeWrapper).inflate(layoutResId, container, false)
  }

  override fun createViewModel() = ViewModelProvider(this)[PersonDetailsViewModel::class.java]

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.loadDetails(person) }
    )
  }

  private fun setupView() {
//    viewPersonDetailsBio.setInitialLines(5)
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    adapter = PersonDetailsAdapter()
    with(personDetailsRecycler) {
      adapter = this@PersonDetailsBottomSheet.adapter
      layoutManager = this@PersonDetailsBottomSheet.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: PersonDetailsUiState) {
    uiState.run {
      personDetails?.let {
        adapter?.setItems(
          listOf(
            PersonDetailsItem.MainInfo(it, dateFormat),
            PersonDetailsItem.MainBio(it.bio ?: "")
          )
        )
      }
    }
//    uiState.run {
//      personDetails?.let { it ->
//        viewPersonDetailsTitle.text = it.name
//        viewPersonDetailsSubtitle.text = it.character
//        viewPersonDetailsBio.visibleIf(!it.bio.isNullOrBlank())
//        viewPersonDetailsBio.text = it.bio
//
//        it.birthday?.let { date ->
//          viewPersonDetailsBirthdayLabel.visible()
//          viewPersonDetailsBirthdayValue.visible()
//          val birthdayText = dateFormat?.format(date)
//            ?.capitalizeWords()
//            ?.plus(it.getAge()?.let { age -> " ($age)" } ?: "")
//            ?.plus(if (!it.birthplace.isNullOrBlank()) "\n${it.birthplace}" else "")
//          viewPersonDetailsBirthdayValue.text = birthdayText
//        }
//        it.deathday?.let { date ->
//          viewPersonDetailsDeathdayLabel.visible()
//          viewPersonDetailsDeathdayValue.visible()
//          viewPersonDetailsDeathdayValue.text = dateFormat?.format(date)?.capitalizeWords()
//        }
//
//        renderImage(it)
//
//        viewPersonDetailsLinkIcon.onClick { _ ->
//          val options = PersonLinksBottomSheet.createBundle(it)
//          navigateTo(R.id.actionPersonDetailsDialogToLinks, options)
//        }
//      }
//      isLoading?.let {
//        viewPersonDetailsProgress.visibleIf(it)
//        viewPersonDetailsLinkIcon.isClickable = !it
//      }
//    }
  }

  private fun renderImage(person: Person) {
//    if (person.imagePath.isNullOrBlank()) {
//      viewPersonDetailsImage.gone()
//      viewPersonDetailsPlaceholder.visible()
//      return
//    }
//
//    viewPersonDetailsImage.visible()
//    viewPersonDetailsPlaceholder.gone()
//
//    Glide.with(this)
//      .load("${Config.TMDB_IMAGE_BASE_ACTOR_URL}${person.imagePath}")
//      .transform(CenterCrop(), GranularRoundedCorners(topLeftCornerRadius, cornerRadius, cornerRadius, cornerRadius))
//      .transition(DrawableTransitionOptions.withCrossFade(Config.IMAGE_FADE_DURATION_MS))
//      .withFailListener {
//        viewPersonDetailsImage.gone()
//        viewPersonDetailsPlaceholder.visible()
//      }
//      .into(viewPersonDetailsImage)
  }

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
