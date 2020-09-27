package com.michaldrabik.ui_base

abstract class UiModel {
  abstract fun update(newModel: UiModel): UiModel
}
