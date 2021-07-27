package org.beckn.one.sandbox.bap.message.factories

import org.beckn.protocol.schemas.ProtocolCategory
import org.beckn.protocol.schemas.ProtocolProviderCatalog

object ProtocolProviderCatalogFactory {

  fun create(index: Int) = ProtocolProviderCatalog(
    id = IdFactory.forProvider(index),
    descriptor = ProtocolDescriptorFactory.create("Retail-provider", IdFactory.forProvider(index)),
    categories = IdFactory.forCategory(IdFactory.forProvider(index), 1).map { ProtocolCategoryFactory.create(it) },
    items = IdFactory.forItems(IdFactory.forProvider(index), 1).map { ProtocolItemFactory.create(it) }
  )
}

object ProtocolCategoryFactory{
  fun create(id: String) = ProtocolCategory(
    id = id,
    descriptor = ProtocolDescriptorFactory.create(id, id),
    tags = mapOf("category-tag1" to "category-value1")
  )

}