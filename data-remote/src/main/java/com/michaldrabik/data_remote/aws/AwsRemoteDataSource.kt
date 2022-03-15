package com.michaldrabik.data_remote.aws

import com.michaldrabik.data_remote.aws.model.AwsImages

/**
 * Fetch/post remote resources via private AWS API
 */
interface AwsRemoteDataSource {
  suspend fun fetchImagesList(): AwsImages
}
