package com.michaldrabik.ui_base.common

import androidx.annotation.StringRes
import com.michaldrabik.ui_base.R

enum class AppCountry(
  val code: String,
  @StringRes val displayName: Int,
  val justWatchQuery: String = "search"
) {
  ARGENTINA("ar", R.string.textCountryArgentina , "buscar"),
  AUSTRALIA("au", R.string.textCountryAustralia),
  AUSTRIA("at", R.string.textCountryAustria, "Suche"),
  BELGIUM("be", R.string.textCountryBelgium, "recherche"),
  BRAZIL("br", R.string.textCountryBrazil, "busca"),
  BULGARIA("bg", R.string.textCountryBulgaria),
  CANADA("ca", R.string.textCountryCanada),
  CHILE("cl", R.string.textCountryChile, "buscar"),
  COLOMBIA("co", R.string.textCountryColombia, "buscar"),
  CZECH_REP("cz", R.string.textCountryCzechRepublic, "vyhledání"),
  DENMARK("dk", R.string.textCountryDenmark),
  ECUADOR("ec", R.string.textCountryEcuador, "buscar"),
  ESTONIA("ee", R.string.textCountryEstonia, "otsing"),
  FINLAND("fi", R.string.textCountryFinland, "etsi"),
  FRANCE("fr", R.string.textCountryFrance, "recherche"),
  GERMANY("de", R.string.textCountryGermany, "Suche"),
  GREECE("gr", R.string.textCountryGreece),
  HUNGARY("hu", R.string.textCountryHungary),
  INDIA("in", R.string.textCountryIndia),
  INDONESIA("id", R.string.textCountryIndonesia),
  IRELAND("ie", R.string.textCountryIreland),
  ITALY("it", R.string.textCountryItaly, "cerca"),
  JAPAN("jp", R.string.textCountryJapan, "検索"),
  LATVIA("lv", R.string.textCountryLatvia),
  LITHUANIA("lt", R.string.textCountryLithuania),
  MALAYSIA("my", R.string.textCountryMalaysia),
  MEXICO("mx", R.string.textCountryMexico, "buscar"),
  NETHERLANDS("nl", R.string.textCountryNetherlands),
  NEW_ZEALAND("nz", R.string.textCountryNewZealand),
  NORWAY("no", R.string.textCountryNorway),
  PERU("pe", R.string.textCountryPeru, "buscar"),
  PHILIPPINES("ph", R.string.textCountryPhilippines),
  POLAND("pl", R.string.textCountryPoland),
  PORTUGAL("pt", R.string.textCountryPortugal, "busca"),
  ROMANIA("ro", R.string.textCountryRomania),
  RUSSIA("ru", R.string.textCountryRussia, "поиск"),
  SINGAPORE("sg", R.string.textCountrySingapore),
  SOUTH_AFRICA("za", R.string.textCountrySouthAfrica),
  SOUTH_KOREA("kr", R.string.textCountrySouthKorea, "검색"),
  SPAIN("es", R.string.textCountrySpain, "buscar"),
  SWEDEN("se", R.string.textCountrySweden),
  SWITZERLAND("ch", R.string.textCountrySwitzerland, "Suche"),
  THAILAND("th", R.string.textCountryThailand),
  TURKEY("tr", R.string.textCountryTurkey, "arama"),
  UKRAINE("ua", R.string.textCountryUkraine, "пошук"),
  UNITED_KINGDOM("uk", R.string.textCountryUnitedKingdom),
  UNITED_STATES("us", R.string.textCountryUnitedStates),
  VENEZUELA("ve", R.string.textCountryVenezuela, "buscar");

  companion object {
    fun fromCode(code: String) = values().first { it.code == code }
  }
}
