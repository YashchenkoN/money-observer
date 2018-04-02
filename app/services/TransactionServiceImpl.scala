package services

import java.util.UUID

import entity.Transaction
import javax.inject.{Inject, Singleton}
import models.security.User
import models.transaction.{CreateTransactionRequest, TransactionView}
import play.api.libs.json.{JsArray, JsObject, Json, OFormat}
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json._
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Mykola Yashchenko
  */
@Singleton
class TransactionServiceImpl @Inject()(reactiveMongoApi: ReactiveMongoApi)
                                      (implicit ec: ExecutionContext) extends TransactionService {

  implicit lazy val format: OFormat[Transaction] = Json.format[Transaction]

  def transactions: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("transactions"))

  override def save(request: CreateTransactionRequest, identity: User): Future[JsObject] = {
    val transaction = Transaction(
      UUID.randomUUID().toString,
      request.accountId,
      identity.id.get,
      request.amount
    )

    transactions.flatMap(_.insert(transaction)).flatMap(_ => Future.successful(Json.obj("id" -> transaction.id)))
  }

  override def delete(id: String, identity: User): Future[Unit] = {
    transactions.flatMap(_.remove(BSONDocument("id" -> id, "userId" -> identity.id)))
      .flatMap(_ => Future.successful(()))
  }

  override def read(id: String, identity: User): Future[JsObject] = {
    transactions.flatMap(_.find(BSONDocument("id" -> id, "userId" -> identity.id)).one[TransactionView])
      .flatMap(acc => Future.successful(Json.toJsObject(acc)))
  }

  override def read(identity: User): Future[JsArray] = {
    transactions.flatMap(
      _.find(BSONDocument("userId" -> identity.id))
        .cursor[TransactionView]()
        .collect[List](100, Cursor.FailOnError[List[TransactionView]]())
    )
      .flatMap(acc =>
        Future.successful(
          JsArray(
            acc.map(tr => Json.toJsObject(tr))
          )
        )
      )
  }
}
