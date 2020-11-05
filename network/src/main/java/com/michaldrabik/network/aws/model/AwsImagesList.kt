package com.michaldrabik.network.aws.model

data class AwsImagesList(
  val shows: AwsImages
)

data class AwsImages(
  val posters: List<AwsImage>,
  val fanarts: List<AwsImage>
)
