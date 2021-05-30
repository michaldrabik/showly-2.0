package com.michaldrabik.ui_streamings.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_model.StreamingService
import com.michaldrabik.ui_streamings.R
import kotlinx.android.synthetic.main.view_streaming.view.*

class StreamingView : FrameLayout {

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
    inflate(context, R.layout.view_streaming, this)
    layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
  }

  fun bind(streaming: StreamingService) {
    this.streaming = streaming
    viewStreamingName.text = streaming.name
    viewStreamingOptions.text = streaming.options.joinToString(", ") { context.getString(it.resId) }

    val corners = if (streaming.name == "Apple iTunes") cornersAppleTransformation else cornersTransformation
    Glide.with(this)
      .load("${Config.TMDB_IMAGE_BASE_LOGO_URL}${streaming.imagePath}")
      .transform(centerCropTransformation, corners)
      .into(viewStreamingImage)
  }
}
