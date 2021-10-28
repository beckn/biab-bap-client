package org.beckn.one.sandbox.bap.configurations

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoDatabase
import org.beckn.one.sandbox.bap.message.entities.AccountDetailsDao
import org.beckn.one.sandbox.bap.message.entities.AddDeliveryAddressDao
import org.beckn.one.sandbox.bap.message.entities.BillingDetailsDao
import org.beckn.one.sandbox.bap.message.entities.OrderDao
import org.beckn.one.sandbox.bap.message.repositories.BecknResponseRepository
import org.beckn.one.sandbox.bap.message.repositories.GenericRepository
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollectionOfName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DatabaseConfiguration @Autowired constructor(
  @Value("\${database.mongo.url}") private val connectionString: String,
  @Value("\${database.mongo.name}") private val databaseName: String
) {
  @Bean
  fun database(): MongoDatabase {
    val settings = MongoClientSettings.builder()
      .applyConnectionString(ConnectionString(connectionString))
      .build()
    val client = KMongo.createClient(settings)
    return client.getDatabase(databaseName)
  }

  @Bean
  fun createOrderDb(@Autowired database: MongoDatabase): BecknResponseRepository<OrderDao> =
    BecknResponseRepository(database.getCollectionOfName("order"))

  @Bean
  fun createDeliverAddressResponseDb(@Autowired database: MongoDatabase): BecknResponseRepository<AddDeliveryAddressDao> =
    BecknResponseRepository(database.getCollectionOfName("delivery_address"))


  @Bean
  fun createBillingResponseDb(@Autowired database: MongoDatabase): BecknResponseRepository<BillingDetailsDao> =
    BecknResponseRepository(database.getCollectionOfName("billing"))

  @Bean
  fun createAccountDetailDb(@Autowired database: MongoDatabase): BecknResponseRepository<AccountDetailsDao> =
    BecknResponseRepository(database.getCollectionOfName("user"))

}
