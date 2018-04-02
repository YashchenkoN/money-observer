package services

import models.security.User
import models.transaction.CreateTransactionRequest
import play.api.libs.json.{JsArray, JsObject}

import scala.concurrent.Future

/**
  * @author Mykola Yashchenko
  */
trait TransactionService {
  def save(request: CreateTransactionRequest, identity: User): Future[JsObject]

  def delete(id: String, identity: User): Future[Unit]

  def read(id: String, identity: User): Future[JsObject]

  // todo add pagination
  def read(identity: User): Future[JsArray]
}
