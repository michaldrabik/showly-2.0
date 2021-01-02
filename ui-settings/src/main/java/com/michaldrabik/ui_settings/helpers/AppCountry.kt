package com.michaldrabik.ui_settings.helpers

enum class AppCountry(
  val code: String,
  val displayName: String
) {
  ARGENTINA("ar", "Argentina"),
  AUSTRALIA("au", "Australia"),
  AUSTRIA("at", "Austria"),
  BELGIUM("be", "Belgium"),
  BRAZIL("br", "Brazil"),
  CANADA("ca", "Canada"),
  CHILE("cl", "Chile"),
  COLOMBIA("co", "Colombia"),
  CZECH_REP("cz", "Czech Republic"),
  DENMARK("dk", "Denmark"),
  ECUADOR("ec", "Ecuador"),
  ESTONIA("ee", "Estonia"),
  FINLAND("fi", "Finland"),
  FRANCE("fr", "France"),
  GERMANY("de", "Germany"),
  GREECE("gr", "Greece"),
  HUNGARY("hu", "Hungary"),
  INDIA("in", "India"),
  INDONESIA("id", "Indonesia"),
  IRELAND("ie", "Ireland"),
  ITALY("it", "Italy"),
  JAPAN("jp", "Japan"),
  LATVIA("lv", "Latvia"),
  LITHUANIA("lt", "Lithuania"),
  MALAYSIA("my", "Malaysia"),
  MEXICO("mx", "Mexico"),
  NETHERLANDS("nl", "Netherlands"),
  NEW_ZEALAND("nz", "New Zealand"),
  NORWAY("no", "Norway"),
  PERU("pe", "Peru"),
  PHILIPPINES("ph", "Philippines"),
  POLAND("pl", "Poland"),
  PORTUGAL("pt", "Portugal"),
  ROMANIA("ro", "Romania"),
  RUSSIA("ru", "Russia"),
  SINGAPORE("sg", "Singapore"),
  SOUTH_AFRICA("za", "South Africa"),
  SOUTH_KOREA("kr", "South Korea"),
  SPAIN("es", "Spain"),
  SWEDEN("se", "Sweden"),
  SWITZERLAND("ch", "Switzerland"),
  THAILAND("th", "Thailand"),
  TURKEY("tr", "Turkey"),
  UNITED_KINGDOM("uk", "United Kingdom"),
  UNITED_STATES("us", "United States"),
  VENEZUELA("ve", "Venezuela");

  companion object {
    fun fromCode(code: String) = values().first { it.code == code }
  }
}
