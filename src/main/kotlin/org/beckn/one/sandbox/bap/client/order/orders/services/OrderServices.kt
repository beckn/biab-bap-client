package org.beckn.one.sandbox.bap.client.order.orders.services

import arrow.core.Either
import org.beckn.one.sandbox.bap.auth.utils.SecurityUtil
import org.beckn.one.sandbox.bap.client.shared.dtos.OrderResponse
import org.beckn.one.sandbox.bap.client.shared.errors.CartError
import org.beckn.one.sandbox.bap.errors.database.DatabaseError
import org.beckn.one.sandbox.bap.message.entities.OrderDao
import org.beckn.one.sandbox.bap.message.repositories.BecknResponseRepository
import org.beckn.one.sandbox.bap.message.repositories.GenericRepository
import org.beckn.one.sandbox.bap.message.services.ResponseStorageService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class OrderServices @Autowired constructor(
  val ordersResponseRepository: ResponseStorageService<OrderResponse,OrderDao>
) {

  val log: Logger = LoggerFactory.getLogger(this::class.java)


  fun findAllOrders(orderId: String, skip: Int = 0, limit: Int  =10):Either<DatabaseError,List<OrderResponse>>{
    val user = SecurityUtil.getSecuredUserDetail()
    return if( user != null){
     if(!orderId.isNullOrEmpty()){
      ordersResponseRepository.findOrdersById(orderId,0,1)
    }else{
      ordersResponseRepository.findManyByUserId(user.uid!!,skip,limit)
      }
    }else{
        Either.Left(DatabaseError.NotFound)
    }
  }
}
