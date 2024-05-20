package com.michaldrabik.ui_base.common.sheets.date_selection

import android.os.Bundle
import android.os.Parcelable
import android.text.format.DateFormat
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.common.extensions.toUtcZone
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.databinding.ViewDateSelectionBinding
import com.michaldrabik.ui_base.utilities.TipsHost
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.Tip.DATE_SELECTION_DEFAULTS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Calendar

@AndroidEntryPoint
class DateSelectionBottomSheet : BaseBottomSheetFragment(R.layout.view_date_selection) {

  companion object {
    const val REQUEST_DATE_SELECTION = "REQUEST_DATE_SELECTION"
    const val RESULT_DATE_SELECTION = "RESULT_DATE_SELECTION"
  }

  private val binding by viewBinding(ViewDateSelectionBinding::bind)

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupTip()
  }

  private fun setupView() {
    with(binding) {
      dateNowButton.onClick {
        closeSheet()
        setFragmentResult(
          requestKey = REQUEST_DATE_SELECTION,
          result = bundleOf(RESULT_DATE_SELECTION to Result.Now)
        )
      }
      dateCustomButton.onClick { openDateSelectionDialog() }
      cancelButton.onClick { closeSheet() }
    }
  }

  private fun setupTip() {
    val isShown = (requireActivity() as TipsHost).isTipShown(DATE_SELECTION_DEFAULTS)
    with(binding) {
      defaultsTipText.visibleIf(!isShown)
      defaultsTipOkButton.visibleIf(!isShown)
      if (!isShown) {
        defaultsTipOkButton.onClick {
          (requireActivity() as TipsHost).setTipShow(DATE_SELECTION_DEFAULTS)
          setupTip()
        }
      }
    }
  }

  private fun openDateSelectionDialog() {
    val now = nowUtc().toLocalZone()
    val firstDayOfWeek = Calendar.getInstance().firstDayOfWeek
    val dialog = MaterialDatePicker.Builder.datePicker()
      .setCalendarConstraints(
        CalendarConstraints.Builder()
          .setFirstDayOfWeek(firstDayOfWeek)
          .build()
      )
      .setTheme(R.style.ShowlyDatePicker)
      .setSelection(now.toMillis())
      .build()
    dialog.addOnPositiveButtonClickListener { dateMillis ->
      openTimeSelectionDialog(now, dateMillis)
    }
    dialog.show(childFragmentManager, "DatePicker")
  }

  private fun openTimeSelectionDialog(now: ZonedDateTime, selectedDate: Long) {
    val is24HourFormat = DateFormat.is24HourFormat(requireContext())
    val dialog = MaterialTimePicker.Builder()
      .setTheme(R.style.ShowlyTimePicker)
      .setTimeFormat(if (is24HourFormat) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
      .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
      .setHour(now.hour)
      .setMinute(now.minute)
      .build()
    dialog.addOnPositiveButtonClickListener {
      onDateTimeSelected(
        selectedDate = selectedDate,
        selectedHour = dialog.hour,
        selectedMinute = dialog.minute
      )
    }
    dialog.show(childFragmentManager, "TimePicker")
  }

  private fun onDateTimeSelected(
    selectedDate: Long,
    selectedHour: Int,
    selectedMinute: Int
  ) {
    val dateUtc = ZonedDateTime.ofInstant(Instant.ofEpochMilli(selectedDate), ZoneId.systemDefault())
      .withHour(selectedHour)
      .withMinute(selectedMinute)
      .toUtcZone()

    closeSheet()

    val result = bundleOf(RESULT_DATE_SELECTION to Result.CustomDate(dateUtc))
    setFragmentResult(REQUEST_DATE_SELECTION, result)
  }

  sealed interface Result : Parcelable {

    @Parcelize
    data object Now : Result

    @Parcelize
    data class CustomDate(val date: ZonedDateTime) : Result
  }
}
