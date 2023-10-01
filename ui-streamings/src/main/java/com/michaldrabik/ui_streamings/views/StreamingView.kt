package com.michaldrabik.ui_streamings.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.openWebUrl
import com.michaldrabik.ui_model.StreamingService
import com.michaldrabik.ui_streamings.R
import com.michaldrabik.ui_streamings.databinding.ViewStreamingBinding

class StreamingView : FrameLayout {

  companion object {
    private const val NETFLIX = "Netflix"
    private const val NETFLIX_FREE = "Netflix Free"
    private const val GOOGLE_PLAY = "Google Play Movies"
    private const val YOU_TUBE = "YouTube"
    private const val AMAZON = "Amazon Prime Video"
    private const val DISNEY = "Disney Plus"
    private const val HBO = "HBO Max"
  }

  private val binding = ViewStreamingBinding.inflate(LayoutInflater.from(context), this)

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val cornerRadius by lazy { context.dimenToPx(R.dimen.streamingImageCorner) }
  private val cornerAppleRadius by lazy { context.dimenToPx(R.dimen.streamingImageCornerApple) }

  private val centerCropTransformation by lazy { CenterCrop() }
  private val cornersTransformation by lazy { RoundedCorners(cornerRadius) }
  private val cornersAppleTransformation by lazy { RoundedCorners(cornerAppleRadius) }

  private lateinit var streaming: StreamingService

  init {
    layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    binding.viewStreamingContent.onClick {
      when (streaming.name) {
        NETFLIX, NETFLIX_FREE -> openWebUrl("https://www.netflix.com/search/${streaming.mediaName}")
        GOOGLE_PLAY -> openWebUrl("https://tv.google.com/")
        YOU_TUBE -> openWebUrl("https://www.youtube.com/results?search_query=${streaming.mediaName} movie")
        AMAZON -> openWebUrl("https://www.primevideo.com/search?phrase=${streaming.mediaName}")
        DISNEY -> openWebUrl("https://www.disneyplus.com/")
        HBO -> openWebUrl("https://play.hbomax.com/")
        else -> openWebUrl(streaming.link)
      }
    }
  }

  fun bind(streaming: StreamingService) {
    this.streaming = streaming
    with(binding) {
      viewStreamingName.text = streaming.name
      viewStreamingOptions.text = streaming.options.joinToString(", ") { context.getString(it.resId) }

      val corners = if (streaming.name == "Apple iTunes") cornersAppleTransformation else cornersTransformation
      Glide.with(this@StreamingView)
        .load("${Config.TMDB_IMAGE_BASE_LOGO_URL}${streaming.imagePath}")
        .transform(centerCropTransformation, corners)
        .into(viewStreamingImage)
    }
  }
}
