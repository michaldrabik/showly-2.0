package com.michaldrabik.ui_base.common.sheets.ratings

import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet.Options.Operation
import com.michaldrabik.ui_base.utilities.Event

data class FinishUiEvent(val operation: Operation) : Event<Operation>(operation)
