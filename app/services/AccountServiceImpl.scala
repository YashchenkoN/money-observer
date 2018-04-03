package services

import java.util.UUID

import entity.Account
import javax.inject.{Inject, Singleton}
import models.account.{AccountView, CreateAccountRequest}
import models.security.User
import play.api.libs.json.{JsObject, Json, OFormat}
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

  implicit lazy val format: OFormat[Account] = Json.format[Account]

  def accounts: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("accounts"))

  override def save(createAccountRequest: CreateAccountRequest, identity: User): Future[JsObject] = {
    val account = Account(
      UUID.randomUUID().toString,
      createAccountRequest.name,
      createAccountRequest.`type`,
      identity.id.get
    )

    accounts
      .flatMap(
        _.find(BSONDocument("name" -> account.name)).one[Account])
          .flatMap {
            case Some(_) => throw new RuntimeException("Account with such name already exists")
            case None =>
              accounts
                .flatMap(_.insert(account))
                .flatMap(_ => Future.successful(Json.obj("id" -> account.id)))
    }
  }

  override def delete(id: String, identity: User): Future[Unit] = {
    accounts
      .flatMap(_.remove(BSONDocument("id" -> id, "userId" -> identity.id)))
      .flatMap(_ => Future.successful(()))
  }

  override def read(id: String, identity: User): Future[JsObject] = {
    accounts
      .flatMap(_.find(BSONDocument("id" -> id, "userId" -> identity.id)).one[AccountView])
      .flatMap(acc => Future.successful(Json.toJsObject(acc)))
  }
}
