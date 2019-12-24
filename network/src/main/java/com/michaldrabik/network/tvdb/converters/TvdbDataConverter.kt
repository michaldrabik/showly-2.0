package com.michaldrabik.network.tvdb.converters

interface TvdbDataConverter<Json, Result> {

  fun fromJson(json: Json): Result
}
