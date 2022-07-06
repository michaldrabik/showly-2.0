import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import com.michaldrabik.ui_model.AirTime
import com.michaldrabik.ui_model.IdImdb
import com.michaldrabik.ui_model.IdSlug
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.IdTvRage
import com.michaldrabik.ui_model.IdTvdb
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily
import com.michaldrabik.ui_model.ImageSource
import com.michaldrabik.ui_model.ImageStatus
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.ShowStatus

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
      title = "DISCOVER_LIST_ITEM",
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
      createdAt = 0,
      updatedAt = 0
    ),
    image = Image(
      id = 0,
      idTvdb = IdTvdb(id = 0),
      idTmdb = IdTmdb(id = 0),
      type = ImageType.POSTER,
      family = ImageFamily.SHOW,
      fileUrl = "",
      thumbnailUrl = "",
      status = ImageStatus.UNKNOWN,
      source = ImageSource.TVDB
    ),
    isLoading = false, isFollowed = false, isWatchlist = false
  )
}
