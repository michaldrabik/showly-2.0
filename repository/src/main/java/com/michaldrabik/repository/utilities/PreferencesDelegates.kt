package com.michaldrabik.repository.utilities

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class StringPreference(
  private val sharedPreferences: SharedPreferences,
  private val key: String,
  private val defaultValue: String,
) : ReadWriteProperty<Any, String> {

  override fun getValue(thisRef: Any, property: KProperty<*>): String =
    sharedPreferences.getString(key, defaultValue) ?: defaultValue

  override fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
    sharedPreferences.edit()
      .putString(key, value)
      .apply()
  }
}

class BooleanPreference(
  private val sharedPreferences: SharedPreferences,
  private val key: String,
  private val defaultValue: Boolean = false,
) : ReadWriteProperty<Any, Boolean> {

  override fun getValue(thisRef: Any, property: KProperty<*>): Boolean =
    sharedPreferences.getBoolean(key, defaultValue)

  override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) =
    sharedPreferences.edit()
      .putBoolean(key, value)
      .apply()
}

class LongPreference(
  private val sharedPreferences: SharedPreferences,
  private val key: String,
  private val defaultValue: Long = 0,
) : ReadWriteProperty<Any, Long> {

  override fun getValue(thisRef: Any, property: KProperty<*>): Long =
    sharedPreferences.getLong(key, defaultValue)

  override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) =
    sharedPreferences.edit()
      .putLong(key, value)
      .apply()
}

class EnumPreference<T : Enum<T>>(
  private val sharedPreferences: SharedPreferences,
  private val key: String,
  private val defaultValue: T,
  private val clazz: Class<T>
) : ReadWriteProperty<Any, T> {

  @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
  override fun getValue(thisRef: Any, property: KProperty<*>): T {
    val enumName = sharedPreferences.getString(key, "")
    return clazz.enumConstants.find { it.name == enumName } ?: defaultValue
  }

  override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
    sharedPreferences.edit()
      .putString(key, value.name)
      .apply()
  }
}
