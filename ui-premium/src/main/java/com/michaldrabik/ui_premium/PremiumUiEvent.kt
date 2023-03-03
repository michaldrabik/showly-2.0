// ktlint-disable filename
package com.michaldrabik.ui_premium

import com.michaldrabik.ui_base.utilities.events.Event

internal sealed class PremiumUiEvent<T>(action: T) : Event<T>(action) {
  data class HighlightItem(val item: com.michaldrabik.ui_model.PremiumFeature) : PremiumUiEvent<Unit>(Unit)
}
