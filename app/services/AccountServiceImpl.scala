package services

import java.util.UUID
import javax.inject.{Inject, Singleton}

import entity.Account
import models.account.CreateAccountRequest
import models.security.User
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json._
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Mykola Yashchenko
  */
@Singleton
class AccountServiceImpl @Inject()(reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) extends AccountService {

  implicit lazy val format = Json.format[Account]

  def accounts: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("accounts"))

  override def save(createAccountRequest: CreateAccountRequest, identity: User): Future[JsObject] = {
    val account = Account(
      UUID.randomUUID().toString,
      createAccountRequest.name,
      createAccountRequest.`type`,
      identity.id.get
    )

    accounts.flatMap(_.find(BSONDocument("name" -> account.name)).one[Account]).flatMap {
      case Some(_) => throw new RuntimeException("Account with such name already exists")
      case None =>
        accounts.flatMap(_.insert(account)).flatMap(_ => Future.successful(Json.obj("id" -> account.id)))
    }
  }

  override def delete(id: String): Future[Unit] = {
    accounts.flatMap(_.remove(BSONDocument("id" -> id))).flatMap(_ => Future.successful(()))
  }
}
