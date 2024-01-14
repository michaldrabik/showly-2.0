package com.michaldrabik.ui_base.common

enum class AppCountry(
  val code: String,
  val displayName: String,
  val justWatchQuery: String = "search"
) {
  ARGENTINA("ar", "Argentina", "buscar"),
  AUSTRALIA("au", "Australia"),
  AUSTRIA("at", "Austria", "Suche"),
  BELGIUM("be", "Belgium", "recherche"),
  BRAZIL("br", "Brazil", "busca"),
  BULGARIA("bg", "Bulgaria"),
  CANADA("ca", "Canada"),
  CHILE("cl", "Chile", "buscar"),
  COLOMBIA("co", "Colombia", "buscar"),
  CZECH_REP("cz", "Czech Republic", "vyhledání"),
  DENMARK("dk", "Denmark"),
  ECUADOR("ec", "Ecuador", "buscar"),
  ESTONIA("ee", "Estonia", "otsing"),
  FINLAND("fi", "Finland", "etsi"),
  FRANCE("fr", "France", "recherche"),
  GERMANY("de", "Germany", "Suche"),
  GREECE("gr", "Greece"),
  HUNGARY("hu", "Hungary"),
  INDIA("in", "India"),
  INDONESIA("id", "Indonesia"),
  IRELAND("ie", "Ireland"),
  ITALY("it", "Italy", "cerca"),
  JAPAN("jp", "Japan", "検索"),
  LATVIA("lv", "Latvia"),
  LITHUANIA("lt", "Lithuania"),
  MALAYSIA("my", "Malaysia"),
  MEXICO("mx", "Mexico", "buscar"),
  NETHERLANDS("nl", "Netherlands"),
  NEW_ZEALAND("nz", "New Zealand"),
  NORWAY("no", "Norway"),
  PERU("pe", "Peru", "buscar"),
  PHILIPPINES("ph", "Philippines"),
  POLAND("pl", "Poland"),
  PORTUGAL("pt", "Portugal", "busca"),
  ROMANIA("ro", "Romania"),
  RUSSIA("ru", "Russia", "поиск"),
  SINGAPORE("sg", "Singapore"),
  SOUTH_AFRICA("za", "South Africa"),
  SOUTH_KOREA("kr", "South Korea", "검색"),
  SPAIN("es", "Spain", "buscar"),
  SWEDEN("se", "Sweden"),
  SWITZERLAND("ch", "Switzerland", "Suche"),
  THAILAND("th", "Thailand"),
  TURKEY("tr", "Turkey", "arama"),
  UNITED_KINGDOM("uk", "United Kingdom"),
  UNITED_STATES("us", "United States"),
  VENEZUELA("ve", "Venezuela", "buscar");

  companion object {
    fun fromCode(code: String) = values().first { it.code == code }
  }
}
