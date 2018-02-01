package services

import java.util.UUID
import javax.inject.{Inject, Singleton}

import entity.Account
import models.account.CreateAccountRequest
import models.security.User
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future

/**
  * @author Mykola Yashchenko
  */
@Singleton
class AccountServiceImpl @Inject()(reactiveMongoApi: ReactiveMongoApi) extends AccountService {

  def accounts: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("accounts"))

  override def save(createAccountRequest: CreateAccountRequest, identity: User): Future[JsObject] = {
    val account = Account(
      UUID.randomUUID().toString,
      createAccountRequest.name,
      createAccountRequest.`type`,
      identity.id.get
    )

    accounts.flatMap(_.find(Json.obj("name" -> account.name)).one[Account]).flatMap {
      case Some(value) => throw new RuntimeException("Account with such name already exists")
      case None =>
        val accountJson = Json.toJson(account)
        accounts.flatMap(_.insert(accountJson)).flatMap(_ => Future.successful(Json.obj("id" -> account.id)))
    }
  }

  override def delete(id: String): Future[Unit] = {
    accounts.flatMap(_.remove(Json.obj("id" -> id))).flatMap(_ => Future.successful(()))
  }
}
