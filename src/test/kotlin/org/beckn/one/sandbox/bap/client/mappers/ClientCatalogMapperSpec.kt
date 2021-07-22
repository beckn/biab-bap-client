package org.beckn.one.sandbox.bap.client.mappers

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.beckn.one.sandbox.bap.client.discovery.mappers.ClientCatalogMapper
import org.beckn.one.sandbox.bap.client.shared.dtos.ClientCatalog
import org.beckn.one.sandbox.bap.message.factories.*
import org.beckn.protocol.schemas.ProtocolProviderCatalog
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@ActiveProfiles(value = ["test"])
@TestPropertySource(locations = ["/application-test.yml"])
class ClientCatalogMapperSpec @Autowired constructor(
  private val clientCatalogMapper: ClientCatalogMapper
) : DescribeSpec() {
  init {
    describe("ClientCatalogMapper") {
      it("should map entity to client") {
        val protocolCatalog = ProtocolCatalogFactory.create()

        val clientDto = clientCatalogMapper.protocolToClientDto(protocolCatalog)

        clientDto shouldBe ClientCatalog(
          bppProviders = listOf(
            ProtocolProviderCatalog(
              id = IdFactory.forProvider(1),
              descriptor = ProtocolDescriptorFactory.create("Retail-provider", IdFactory.forProvider(1)),
              categories = IdFactory.forCategory(IdFactory.forProvider(1), 1)
                .map { ProtocolCategoryFactory.create(it) },
              items = IdFactory.forItems(IdFactory.forProvider(1), 1).map { ProtocolItemFactory.create(it) }
            )
          )
        )
      }
    }
  }

}