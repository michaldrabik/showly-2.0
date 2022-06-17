package com.michaldrabik.ui_model

enum class Network(
  vararg val channels: String,
) {
  AMAZON(
    "Amazon",
    "Amazon (Japan)",
    "Amazon Freevee",
    "Amazon Kids+",
    "Amazon Prime Video",
    "Amazon Studios",
    "Amazon miniTV"
  ),
  DISNEY(
    "Disney",
    "Disney Channel",
    "Disney Channel (DE)",
    "Disney Channel (FR)",
    "Disney Channel (IT)",
    "Disney Channel (UK)",
    "Disney Channel (US)",
    "Disney Cinemagic",
    "Disney Junior",
    "Disney Junior (UK)",
    "Disney Television Animation",
    "Disney XD",
    "Disney XD (Latin America)",
    "Disney+",
    "Disney+ Hotstar"
  ),
  HBO(
    "HBO",
    "HBO Asia",
    "HBO Brasil",
    "HBO Canada",
    "HBO España",
    "HBO Europe",
    "HBO Family",
    "HBO Latin",
    "HBO America",
    "HBO Magyarország",
    "HBO Max",
    "HBO Nordic",
  ),
  HULU("Hulu", "Hulu Japan"),
  ITV(
    "ITV",
    "ITV Encore",
    "ITV Granada",
    "ITV Wales"
  ),
  NETFLIX("Netflix")
}
