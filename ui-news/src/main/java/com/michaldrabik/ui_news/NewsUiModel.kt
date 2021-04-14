package com.michaldrabik.ui_news

import com.michaldrabik.ui_base.UiModel
import com.michaldrabik.ui_model.NewsItem

data class NewsUiModel(
  val items: List<NewsItem>? = null,
  val isLoading: Boolean? = null,
) : UiModel() {

  override fun update(newModel: UiModel) =
    (newModel as NewsUiModel).copy(
      items = newModel.items?.toList() ?: items,
      isLoading = newModel.isLoading ?: isLoading,
    )
}
