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
import com.michaldrabik.common.extensions.dateFromMillis
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.common.extensions.toUtcZone
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.databinding.ViewDateSelectionBinding
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.utilities.TipsHost
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.requireSerializable
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.Tip.DATE_SELECTION_DEFAULTS
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_OPTIONS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@AndroidEntryPoint
class DateSelectionBottomSheet : BaseBottomSheetFragment(R.layout.view_date_selection) {

  companion object {
    const val REQUEST_DATE_SELECTION = "REQUEST_DATE_SELECTION"
    const val RESULT_DATE_SELECTION = "RESULT_DATE_SELECTION"

    fun createBundle(releaseDate: ZonedDateTime?): Bundle =
      bundleOf(
        ARG_OPTIONS to releaseDate,
      )
  }

  private val binding by viewBinding(ViewDateSelectionBinding::bind)
  private val releaseDate by lazy { requireSerializable<ZonedDateTime?>(ARG_OPTIONS) }

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupTip()
  }

  private fun setupView() {
    with(binding) {
      cancelButton.onClick { closeSheet() }
      dateNowButton.onClick {
        closeSheet()
        setFragmentResult(
          requestKey = REQUEST_DATE_SELECTION,
          result = bundleOf(RESULT_DATE_SELECTION to Result.Now),
        )
      }
      dateCustomButton.onClick { openDateSelectionDialog() }
      with(dateReleaseButton) {
        if (releaseDate != null) {
          isEnabled = true
          alpha = 1F
          val dateFormat = DateTimeFormatter.ofPattern(DateFormatProvider.DAY_1)
          dateReleaseButtonLabel.text = releaseDate?.toLocalZone()?.format(dateFormat)
          dateReleaseButtonLabel.visible()
        } else {
          isEnabled = false
          alpha = 0.3F
          dateReleaseButtonLabel.text = null
          dateReleaseButtonLabel.gone()
        }
        onClick { onReleaseDateSelected() }
      }
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
    val dialog = MaterialDatePicker.Builder
      .datePicker()
      .setCalendarConstraints(
        CalendarConstraints
          .Builder()
          .setFirstDayOfWeek(Calendar.MONDAY)
          .build(),
      ).setTheme(R.style.ShowlyDatePicker)
      .setSelection(now.toMillis() + (now.offset.totalSeconds * 1000))
      .build()
    dialog.addOnPositiveButtonClickListener {
      openTimeSelectionDialog(now, dateFromMillis(it).withZoneSameLocal(now.zone))
    }
    dialog.show(childFragmentManager, "DatePicker")
  }

  private fun openTimeSelectionDialog(
    now: ZonedDateTime,
    selectedDate: ZonedDateTime,
  ) {
    val is24HourFormat = DateFormat.is24HourFormat(requireContext())

    val dialog = MaterialTimePicker
      .Builder()
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
        selectedMinute = dialog.minute,
      )
    }

    dialog.show(childFragmentManager, "TimePicker")
  }

  private fun onDateTimeSelected(
    selectedDate: ZonedDateTime,
    selectedHour: Int,
    selectedMinute: Int,
  ) {
    val resultDate = selectedDate
      .withHour(selectedHour)
      .withMinute(selectedMinute)
      .toUtcZone()

    closeSheet()

    val result = bundleOf(RESULT_DATE_SELECTION to Result.CustomDate(resultDate))
    setFragmentResult(REQUEST_DATE_SELECTION, result)
  }

  private fun onReleaseDateSelected() {
    val resultDate = releaseDate
      ?.toLocalZone()
      ?.withHour(20)
      ?.withMinute(0)
      ?.toUtcZone()
      ?: nowUtc()

    closeSheet()

    setFragmentResult(
      requestKey = REQUEST_DATE_SELECTION,
      result = bundleOf(
        RESULT_DATE_SELECTION to Result.ReleaseDate(resultDate),
      ),
    )
  }

  sealed interface Result : Parcelable {

    @Parcelize
    data object Now : Result

    @Parcelize
    data class ReleaseDate(
      val date: ZonedDateTime,
    ) : Result

    @Parcelize
    data class CustomDate(
      val date: ZonedDateTime,
    ) : Result
  }
}
