package org.beckn.one.sandbox.bap.message.entities

import org.beckn.one.sandbox.bap.Default
import java.time.LocalDateTime

data class ItemDao @Default constructor(
  val id: String? = null,
  val parentItemId: String? = null,
  val descriptor: DescriptorDao? = null,
  val price: PriceDao? = null,
  val categoryId: String? = null,
  val locationId: String? = null,
  val time: LocalDateTime? = null,
  val matched: Boolean? = null,
  val related: Boolean? = null,
  val recommended: Boolean? = null,
  val tags: Map<String, String>? = null
)