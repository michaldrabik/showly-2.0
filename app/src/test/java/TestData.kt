import com.michaldrabik.showly2.model.AirTime
import com.michaldrabik.showly2.model.IdImdb
import com.michaldrabik.showly2.model.IdSlug
import com.michaldrabik.showly2.model.IdTmdb
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.IdTvRage
import com.michaldrabik.showly2.model.IdTvdb
import com.michaldrabik.showly2.model.Ids
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageFamily
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.ShowStatus
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem

object TestData {

  val DISCOVER_LIST_ITEM = DiscoverListItem(
    show = Show(
      ids = Ids(
        trakt = IdTrakt(id = 0),
        slug = IdSlug(id = ""),
        tvdb = IdTvdb(id = 0),
        imdb = IdImdb(id = ""),
        tmdb = IdTmdb(id = 0),
        tvrage = IdTvRage(id = 0)
      ),
      title = "",
      year = 0,
      overview = "",
      firstAired = "",
      runtime = 0,
      airTime = AirTime(day = "", time = "", timezone = ""),
      certification = "",
      network = "",
      country = "",
      trailer = "",
      homepage = "",
      status = ShowStatus.UNKNOWN,
      rating = 0.0f,
      votes = 0,
      commentCount = 0,
      genres = listOf(),
      airedEpisodes = 0,
      updatedAt = 0
    ), image = Image(
      id = 0,
      idTvdb = IdTvdb(id = 0),
      type = ImageType.POSTER,
      family = ImageFamily.SHOW,
      fileUrl = "",
      thumbnailUrl = "",
      status = Image.Status.UNKNOWN
    ), isLoading = false, isFollowed = false, isSeeLater = false
  )
}
