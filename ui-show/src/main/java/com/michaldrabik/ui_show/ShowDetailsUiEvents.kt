// ktlint-disable filename
package com.michaldrabik.ui_show

import androidx.annotation.IdRes
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_people.details.PersonDetailsArgs

sealed class ShowDetailsEvent<T>(action: T) : Event<T>(action) {

  data class OpenPersonSheet(
    val show: Show,
    val person: Person,
    val personArgs: PersonDetailsArgs?,
  ) : ShowDetailsEvent<Show>(show)

  data class OpenPeopleSheet(
    val show: Show,
    val people: List<Person>,
    val department: Person.Department,
  ) : ShowDetailsEvent<Show>(show)

  data class RemoveFromTrakt(
    @IdRes val actionId: Int,
    val mode: RemoveTraktBottomSheet.Mode,
    val traktIds: List<IdTrakt>,
  ) : ShowDetailsEvent<Int>(actionId)

  data object RefreshSeasons : ShowDetailsEvent<Unit>(Unit)

  data object Finish : ShowDetailsEvent<Unit>(Unit)
}
