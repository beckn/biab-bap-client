package org.beckn.one.sandbox.bap.client.shared.dtos

import org.beckn.one.sandbox.bap.Default

data class SearchCriteria @Default constructor(
  val searchString: String? = null,
  val location: String? = null,
  val providerId: String? = null,
  val categoryId: String? = null,
  val bppId: String? = null,
)
