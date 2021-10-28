package org.beckn.one.sandbox.bap.client.order.status.services

import arrow.core.Either
import org.beckn.one.sandbox.bap.client.shared.dtos.*
import org.beckn.one.sandbox.bap.errors.database.DatabaseError
import org.beckn.one.sandbox.bap.message.entities.OnConfirmDao
import org.beckn.one.sandbox.bap.message.entities.OrderDao
import org.beckn.one.sandbox.bap.message.mappers.GenericResponseMapper
import org.beckn.one.sandbox.bap.message.services.ResponseStorageService
import org.litote.kmongo.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class OnOrderStatusService @Autowired constructor(
  val repository: ResponseStorageService<OrderResponse, OrderDao>,
  val mapper: GenericResponseMapper<OrderResponse, OrderDao>,
  private val log: Logger = LoggerFactory.getLogger(OnOrderStatusService::class.java)
) {
   fun updateOrder(orderDao: OrderDao):Either<DatabaseError, ClientResponse>{
     return if(orderDao.messageId == null){
       log.error("Message id is not available")
       Either.Left(DatabaseError.NotFound)
     }else{
       log.error("Updating db on order status callback")
       repository.updateDocByQuery(OrderDao::messageId eq orderDao.messageId!!, orderDao)
     }
   }
}
